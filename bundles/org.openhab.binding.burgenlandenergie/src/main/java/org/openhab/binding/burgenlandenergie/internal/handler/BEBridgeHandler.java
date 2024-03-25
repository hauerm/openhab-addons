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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.burgenlandenergie.internal.api.ApiClient;
import org.openhab.binding.burgenlandenergie.internal.api.pojo.ContractAccount;
import org.openhab.binding.burgenlandenergie.internal.config.BEBridgeConfiguration;
import org.openhab.binding.burgenlandenergie.internal.utils.EnvSwitch;
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

    private ContractAccount[] contractAccounts = new ContractAccount[0];

    @Nullable
    private ScheduledFuture<?> pollingJob = null;

    @Nullable
    private ApiClient apiClient;

    public BEBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        BEBridgeConfiguration config = getConfigAs(BEBridgeConfiguration.class);

        // check if the configuration is valid
        if (!config.username.isBlank() && !config.password.isBlank() && !config.customerNr.isBlank()) {
            apiClient = new ApiClient(config);
            pollingJob = scheduler.scheduleWithFixedDelay(this::fetchContractAccounts, 0L,
                    EnvSwitch.refreshIntervallMinutes, MINUTES);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.invalid-configuration");
        }
    }

    private void fetchContractAccounts() {
        try {
            // apiClient cant be null here, but pmd does not recognize it
            if (apiClient != null && getThing().isEnabled()) {
                contractAccounts = apiClient.getContractAccounts().get();

                if (contractAccounts.length > 0) {
                    // request was successfull, set bridge to online
                    updateStatus(ThingStatus.ONLINE);
                }

                getThing().getThings().forEach(thing -> {
                    if (thing.getHandler() instanceof IBEBridgeListener bridgeListener) {
                        bridgeListener.onContractAccountsUpdate(contractAccounts);
                    }
                });
            }
        } catch (InterruptedException | ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.unknown");
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        if (apiClient != null) {
            apiClient.dispose();
            apiClient = null;
        }
        logger.debug("BEBridgeHandler disposed");
    }
}
