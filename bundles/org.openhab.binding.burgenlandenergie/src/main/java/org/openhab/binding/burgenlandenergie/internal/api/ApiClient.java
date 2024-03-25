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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.burgenlandenergie.internal.api.pojo.ContractAccount;
import org.openhab.binding.burgenlandenergie.internal.api.pojo.ContractAccountRequest;
import org.openhab.binding.burgenlandenergie.internal.api.pojo.ContractAccountResponse;
import org.openhab.binding.burgenlandenergie.internal.config.BEBridgeConfiguration;
import org.openhab.binding.burgenlandenergie.internal.utils.EnvSwitch;
import org.openhab.binding.burgenlandenergie.internal.utils.GsonSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP API client which handles oauth2 authentication including token refresh
 * 
 * @author Michael Hauer - Initial contribution
 */
@NonNullByDefault
public class ApiClient {
    private final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private static final String PROTOCOL = "https://";
    private static final String HOST_ID = EnvSwitch.isProd ? "1kchpzz7aa" : "awnl7rwekl";
    private static final String API_HOST = HOST_ID + ".execute-api.eu-central-1.amazonaws.com";
    private static final String API_PATH = (EnvSwitch.isProd ? "/prod" : "/dev") + "/api/sap/action";
    private static final String API_CONTRACT_ACCOUNTS = "/contract-accounts";

    @Nullable
    BEBridgeConfiguration config;
    @Nullable
    ApiAuthenticator authenticator;

    public ApiClient(BEBridgeConfiguration config) {
        this.config = config;
        this.authenticator = new ApiAuthenticator(config.username, config.password);
    }

    /**
     * Fetches all contract accounts from the API.
     *
     * @return CompletableFuture<ContractAccount[]> containing the contract accounts or an empty array if no contract
     *         accounts are available or an error occurred.
     */
    public CompletableFuture<ContractAccount[]> getContractAccounts() {
        if (authenticator != null) {
            return authenticator.getIdToken().thenCompose(this::postContractAccountsRequest);
        } else {
            logger.debug("Could not fetch Contract-Accounts: authenticator is not initialized");
            return CompletableFuture.completedFuture(new ContractAccount[0]);
        }
    }

    private CompletableFuture<ContractAccount[]> postContractAccountsRequest(String idToken) {
        ContractAccountRequest body = new ContractAccountRequest("X", "", "", "X", "X", "", "", "");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROTOCOL + API_HOST + API_PATH + API_CONTRACT_ACCOUNTS))
                .header("Content-Type", "application/json").header("Authorization", idToken)
                .header("Customer-Nr", config.customerNr)
                .POST(HttpRequest.BodyPublishers.ofString(GsonSingleton.INSTANCE.toJson(body))).build();

        CompletableFuture<HttpResponse<String>> futureCAs = HttpClientSingleton.INSTANCE.sendAsync(request,
                HttpResponse.BodyHandlers.ofString());

        return futureCAs.thenApply(response -> {
            if (response.statusCode() == 200) {
                try {
                    ContractAccountResponse caResponse = GsonSingleton.INSTANCE.fromJson(response.body(),
                            ContractAccountResponse.class);

                    if (caResponse != null) {
                        if (!Objects.equals(caResponse.getSapMessage().status(), "S")
                                || caResponse.getContractAccounts().length == 0) {
                            logger.error("{}", caResponse.getSapMessage().text());
                        }

                        logger.debug("Contract-Accounts successfully fetched");
                        return caResponse.getContractAccounts();
                    } else {
                        logger.debug("Error while parsing Contract-Accounts");
                    }
                } catch (Exception e) {
                    logger.error("Error while parsing Contract-Accounts: {}", e.getMessage());
                }

            } else if (response.statusCode() == 401) {
                logger.error(API_CONTRACT_ACCOUNTS + ": unauthorized access");
            } else {
                logger.error("{}: unknown server error", API_CONTRACT_ACCOUNTS);
            }

            return new ContractAccount[0];
        });
    }

    public void dispose() {
        if (authenticator != null) {
            authenticator.dispose();
            authenticator = null;
        }
    }
}
