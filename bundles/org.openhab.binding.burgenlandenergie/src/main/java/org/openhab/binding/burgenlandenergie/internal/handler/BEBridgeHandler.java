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
package org.openhab.binding.burgenlandenergie.internal.handler;

import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.burgenlandenergie.internal.api.ApiClient;
import org.openhab.binding.burgenlandenergie.internal.api.pojo.ContractAccount;
import org.openhab.binding.burgenlandenergie.internal.config.BEBridgeConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BEBridgeHandler} is responsible authentication and handle api-requests for data shared among all things.
 *
 * @author Michael Hauer - Initial contribution
 */
@NonNullByDefault
public class BEBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(BEBridgeHandler.class);

    private final BEBridgeConfiguration config;
    private @Nullable ApiClient apiClient;

    private ContractAccount[] contractAccounts = new ContractAccount[0];

    public BEBridgeHandler(Bridge bridge) {
        super(bridge);
        this.config = getConfigAs(BEBridgeConfiguration.class);
        this.apiClient = new ApiClient(this.config);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        boolean configValid = true;

        // check if the configuration is valid
        if (config.username.isBlank() || config.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.invalid-configuration");
            configValid = false;
        }

        if (configValid && apiClient != null) {
            scheduler.scheduleWithFixedDelay(this::fetchContractAccounts, 0L, 30, MINUTES);
        }
    }

    private void fetchContractAccounts() {
        try {
            // apiClient cant be null here, but pmd does not recognize it
            if (apiClient != null) {
                contractAccounts = apiClient.getContractAccounts().get();

                if (contractAccounts.length > 0) {
                    // request was successfull, set bridge to online
                    updateStatus(ThingStatus.ONLINE);
                }

                getThing().getThings().forEach(thing -> {
                    if (thing.getHandler() instanceof IContractAccountListener) {
                        ((IContractAccountListener) thing.getHandler()).onContractAccountsUpdate(contractAccounts);
                    }
                });
            }
        } catch (InterruptedException | ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
    }
}
