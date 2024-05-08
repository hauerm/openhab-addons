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

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.burgenlandenergie.internal.BEBindingConstants;
import org.openhab.binding.burgenlandenergie.internal.api.enums.Division;
import org.openhab.binding.burgenlandenergie.internal.api.pojo.ContractAccount;
import org.openhab.binding.burgenlandenergie.internal.config.ElectricityTariffThingConfig;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.CurrencyUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;

/**
 * The {@link ElectricityTariffThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Hauer - Initial contribution
 */
@NonNullByDefault
public class ElectricityTariffThingHandler extends BaseThingHandler implements IBEBridgeListener {
    private final ElectricityTariffThingConfig config = getConfigAs(ElectricityTariffThingConfig.class);

    public ElectricityTariffThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        // check if the configuration is valid
        if (config.contractAccountNr.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.invalid-configuration");
        }

        // if bridge-thing is already online we can gracefully set the status to online
        if (getBridge() != null && getBridge().getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void onContractAccountsUpdate(ContractAccount[] contractAccounts) {
        ContractAccount ca = Arrays.stream(contractAccounts)
                .filter(c -> config.contractAccountNr.equals(c.getContractAccountNr())).findFirst().orElse(null);

        if (ca != null) {

            Arrays.stream(ca.getTariffs()).filter(t -> {
                boolean divisionOK = Division.ELECTRICITY.getId().equals(t.division());

                if (divisionOK) {
                    // && config.electricalHeating == t.electricityWarmth();
                    if (config.tariffClassification.equals("HEATING") && t.electricityWarmth()) {
                        return true;
                    } else if (config.tariffClassification.equals("FEEDIN") && ca.isElectricityFeeder()) {
                        return true;
                    } else if (config.tariffClassification.equals("DEFAULT") && !ca.isElectricityFeeder()
                            && !t.electricityWarmth()) {
                        return true;
                    }
                }

                return false;
            }).findFirst().ifPresentOrElse(t -> {
                double taxFactor = ca.isElectricityFeeder() ? 1 : 1.2;
                updateState(BEBindingConstants.TARIFF_DELIVERY_ADDRESS, new StringType(ca.getDescription()));
                updateState(BEBindingConstants.TARIFF_NAME, new StringType(t.tariffName()));
                updateState(BEBindingConstants.TARIFF_PRICE_KWH,
                        new QuantityType<>(t.workPrice() * taxFactor, CurrencyUnits.BASE_ENERGY_PRICE));
                updateState(BEBindingConstants.TARIFF_PRICE_BASE,
                        new QuantityType<>(t.basePrice() * taxFactor, CurrencyUnits.BASE_CURRENCY));
            }, () -> {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.invalid-division");
            });

            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.contract-not-found");
        }
    }
}
