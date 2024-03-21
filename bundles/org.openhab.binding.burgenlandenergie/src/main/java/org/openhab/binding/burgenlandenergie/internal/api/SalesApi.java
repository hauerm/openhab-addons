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
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.burgenlandenergie.internal.api.jsonModels.request.ContractAccountBody;
import org.openhab.binding.burgenlandenergie.internal.config.SalesApiConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * HTTP API client which handles oauth2 authentication including token refresh
 * 
 * @author Michael Hauer - Initial contribution
 */
@NonNullByDefault
public class SalesApi {

    private final Logger logger = LoggerFactory.getLogger(SalesApi.class);
    private static final String PROTOCOLL = "https://";
    private static final String SALES_API_HOST = "1kchpzz7aa.execute-api.eu-central-1.amazonaws.com";
    private static final String SALES_API_PATH = "/prod/api/sap/action";
    private static final String SALES_API_CONTRACT_ACCOUNTS = "/contract-accounts";

    SalesApiConfiguration config;
    SalesApiAuthenticator authenticator;

    public SalesApi(SalesApiConfiguration config) {
        this.config = config;
        this.authenticator = new SalesApiAuthenticator(config.username, config.password);
    }

    public CompletableFuture<JsonObject> getContractAccounts() {
        return authenticator.getAccessToken().thenCompose(this::postContractAccountsRequest);
    }

    private CompletableFuture<JsonObject> postContractAccountsRequest(String accessToken) {
        ContractAccountBody body = new ContractAccountBody("X", "", "", "", "", "", "", "");
        Gson gson = new Gson();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROTOCOLL + SALES_API_HOST + SALES_API_PATH + SALES_API_CONTRACT_ACCOUNTS))
                .header("Content-Type", "application/json").header("Authorization", accessToken)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body))).build();

        CompletableFuture<HttpResponse<String>> futureCAs = HttpClientSingleton.INSTANCE.sendAsync(request,
                HttpResponse.BodyHandlers.ofString());

        return futureCAs.thenApply(response -> {
            if (response.statusCode() == 200) {
                logger.debug("Contract-Accounts successfully fetched");
                return JsonParser.parseString(response.body()).getAsJsonObject();
            } else {
                throw new RuntimeException("Failed to obtain contract accounts");
            }
        });
    }
}
