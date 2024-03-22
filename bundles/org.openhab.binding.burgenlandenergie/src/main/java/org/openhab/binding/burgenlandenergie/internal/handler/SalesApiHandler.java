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

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.burgenlandenergie.internal.BurgenlandEnergieBindingConstants;
import org.openhab.binding.burgenlandenergie.internal.api.SalesApi;
import org.openhab.binding.burgenlandenergie.internal.api.pojo.ContractAccountResponse;
import org.openhab.binding.burgenlandenergie.internal.config.SalesApiConfiguration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.CurrencyUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SalesApiHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Hauer - Initial contribution
 */
@NonNullByDefault
public class SalesApiHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SalesApiHandler.class);

    private @Nullable SalesApi salesApi;
    private SalesApiConfiguration config;

    public SalesApiHandler(Thing thing) {
        super(thing);
        config = getConfigAs(SalesApiConfiguration.class);
        salesApi = new SalesApi(this.config);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        boolean configValid = true;

        if (config.username.isBlank() || config.password.isBlank() || config.contractAccountNr.isBlank()
                || config.customerNr.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.invalid-configuration");
            configValid = false;
        }

        if (configValid && salesApi != null) {
            updateStatus(ThingStatus.UNKNOWN);
            scheduler.scheduleWithFixedDelay(this::refresh, 0L, 10, MINUTES);
        }

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");
        //
        // Logging to INFO should be avoided normally.
        // See https://www.openhab.org/docs/developer/guidelines.html#f-logging
    }

    private void refresh() {
        try {
            ContractAccountResponse caResponse = salesApi.getContractAccounts().get();

            if (!Objects.equals(caResponse.getSapMessage().status(), "S")) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.unknown");
                logger.error("{}", caResponse.getSapMessage().text());
                return;
            }

            updateStatus(ThingStatus.ONLINE);
            Arrays.stream(caResponse.getContractAccounts()).forEach(ca -> {
                Arrays.stream(ca.getTariffs()).filter(t -> "10".equals(t.division())).forEach(t -> {
                    updateState(BurgenlandEnergieBindingConstants.CURRENT_TARIFF_NAME, new StringType(t.tariffName()));
                    updateState(BurgenlandEnergieBindingConstants.CURRENT_TARIFF_PRICE_KWH,
                            new QuantityType<>(t.workPrice() * 1.2, CurrencyUnits.BASE_ENERGY_PRICE));
                    updateState(BurgenlandEnergieBindingConstants.CURRENT_TARIFF_PRICE_BASE,
                            new QuantityType<>(t.basePrice() *1.2, CurrencyUnits.BASE_CURRENCY));
                });
            });

            logger.info("tariff information successfully updated");
        } catch (InterruptedException | ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        salesApi = null;
    }
}
