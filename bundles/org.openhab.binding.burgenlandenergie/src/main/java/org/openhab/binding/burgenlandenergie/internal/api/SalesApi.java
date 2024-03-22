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
import org.openhab.binding.burgenlandenergie.internal.api.pojo.ContractAccountRequest;
import org.openhab.binding.burgenlandenergie.internal.api.pojo.ContractAccountResponse;
import org.openhab.binding.burgenlandenergie.internal.config.SalesApiConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

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

    public CompletableFuture<ContractAccountResponse> getContractAccounts() {
        return authenticator.getIdToken().thenCompose(this::postContractAccountsRequest);
    }

    private CompletableFuture<ContractAccountResponse> postContractAccountsRequest(String idToken) {
        // By the time of implementing this we focus on electricity
        // We plan to include other divisions like gas, emobility, service in the future

        String readGas = config.division.equals("G") ? "X" : "";

        ContractAccountRequest body = new ContractAccountRequest("X", "", "", "", readGas, "", "", "");
        Gson gson = new Gson();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROTOCOLL + SALES_API_HOST + SALES_API_PATH + SALES_API_CONTRACT_ACCOUNTS))
                .header("Content-Type", "application/json").header("Authorization", idToken)
                .header("Customer-Nr", config.customerNr).POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        CompletableFuture<HttpResponse<String>> futureCAs = HttpClientSingleton.INSTANCE.sendAsync(request,
                HttpResponse.BodyHandlers.ofString());

        return futureCAs.thenApply(response -> {
            if (response.statusCode() == 200) {

                try {
                    ContractAccountResponse caResponse = gson.fromJson(response.body(), ContractAccountResponse.class);

                    if (caResponse != null) {
                        logger.debug("Contract-Accounts successfully fetched");
                        return caResponse;
                    } else {
                        throw new RuntimeException("Error while parsing Contract-Accounts");
                    }
                } catch (Exception e) {
                    logger.error("Error while parsing Contract-Accounts: {}", e.getMessage());
                    throw new RuntimeException("Error while parsing Contract-Accounts: {}" + e.getMessage());
                }

            } else if (response.statusCode() == 401) {
                logger.error(SALES_API_CONTRACT_ACCOUNTS + ": unauthorized access");
                throw new RuntimeException(SALES_API_CONTRACT_ACCOUNTS + ": unauthorized access");
            } else {
                logger.error("{}: unknown server error", SALES_API_CONTRACT_ACCOUNTS);
                throw new RuntimeException(SALES_API_CONTRACT_ACCOUNTS + ": unknown server error");
            }
        });
    }
}
