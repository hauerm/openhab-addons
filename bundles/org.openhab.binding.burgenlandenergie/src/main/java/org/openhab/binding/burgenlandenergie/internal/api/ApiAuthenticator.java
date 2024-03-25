/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.burgenlandenergie.internal.api;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.burgenlandenergie.internal.api.pojo.Login;
import org.openhab.binding.burgenlandenergie.internal.api.pojo.OAuthToken;
import org.openhab.binding.burgenlandenergie.internal.utils.EnvSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * HTTP API client which handles oauth2 authentication including token refresh using java.net.HTTP
 * 
 * @author Michael Hauer - Initial contribution
 */
public class ApiAuthenticator {

    private final Logger logger = LoggerFactory.getLogger(ApiAuthenticator.class);
    private final static String AUTH_API_PROTOCOL = "https://";
    private static final String HOST_ID = EnvSwitch.isProd ? "1kchpzz7aa" : "awnl7rwekl";
    private static final String AUTH_API_HOST = HOST_ID + ".execute-api.eu-central-1.amazonaws.com";
    private static final String AUTH_API_PATH = (EnvSwitch.isProd ? "/prod" : "/dev") + "/papi/auth";
    private static final String AUTH_API_LOGIN = "/signin";
    private static final String AUTH_API_REFRESH = "/signin/refresh";

    private final Login login;
    private OAuthToken oAuthToken = null;
    Timer refreshTokenTimer;

    public ApiAuthenticator(String username, String password) {
        this.login = new Login(username, password);
    }

    public CompletableFuture<String> getIdToken() {
        if (oAuthToken == null || oAuthToken.isExpired()) {
            Gson gson = new Gson();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AUTH_API_PROTOCOL + AUTH_API_HOST + AUTH_API_PATH + AUTH_API_LOGIN))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(login))).build();

            CompletableFuture<HttpResponse<String>> futureTokens = HttpClientSingleton.INSTANCE.sendAsync(request,
                    HttpResponse.BodyHandlers.ofString());

            return futureTokens.thenApply(response -> {
                if (response.statusCode() == 200) {
                    JsonObject obj = JsonParser.parseString(response.body()).getAsJsonObject();
                    this.oAuthToken = new OAuthToken(obj.get("idToken").getAsString(),
                            obj.get("accessToken").getAsString(), obj.get("refreshToken").getAsString(),
                            obj.get("expireIn").getAsString());
                    logger.debug("Tokens obtained successfully");
                    refreshAccessTokenHalfHourly();
                    return oAuthToken;
                } else {
                    throw new RuntimeException("Failed to obtain tokens");
                }
            }).thenApply(OAuthToken::getIdToken);
        } else {
            return CompletableFuture.completedFuture(oAuthToken.getIdToken());
        }
    }

    public void refreshAccessToken() {
        // schedule job to refresh token every hour
        CompletableFuture.runAsync(() -> {
            // obtain new tokens
            String refreshTokenJson = "{\"refreshToken\":\"" + oAuthToken.getRefreshToken() + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AUTH_API_PROTOCOL + AUTH_API_HOST + AUTH_API_PATH + AUTH_API_REFRESH))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(refreshTokenJson)).build();

            CompletableFuture<HttpResponse<String>> futureTokens = HttpClientSingleton.INSTANCE.sendAsync(request,
                    HttpResponse.BodyHandlers.ofString());

            futureTokens.thenAccept(response -> {
                if (response.statusCode() == 200) {
                    JsonObject obj = JsonParser.parseString(response.body()).getAsJsonObject();
                    this.oAuthToken = new OAuthToken(obj.get("idToken").getAsString(),
                            obj.get("accessToken").getAsString(), obj.get("refreshToken").getAsString(),
                            obj.get("expireIn").getAsString());
                    logger.debug("Tokens refreshed successfully");
                } else {
                    throw new RuntimeException("Failed to refresh tokens");
                }
            });
        });
    }

    private void refreshAccessTokenHalfHourly() {
        if (refreshTokenTimer == null) {
            refreshTokenTimer = new Timer();
            refreshTokenTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    refreshAccessToken();
                }
            }, TimeUnit.MINUTES.toMillis(EnvSwitch.refreshIntervallMinutes),
                    TimeUnit.MINUTES.toMillis(EnvSwitch.refreshIntervallMinutes));
            logger.debug("Started refresh token timer");
        }
    }

    public void dispose() {
        refreshTokenTimer.cancel();
        int canceledTimerTasks = refreshTokenTimer.purge();
        refreshTokenTimer = null;
        logger.debug("Canceled {} refresh token timer tasks.", canceledTimerTasks);
    }
}
