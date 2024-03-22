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
package org.openhab.binding.burgenlandenergie.internal.api.pojo;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * POJO
 *
 * @author Michael Hauer - Initial contribution
 */
@NonNullByDefault
public class ContractAccount {

    private final String contractAccountNr;
    private final String description;
    private final boolean electricity;
    private final boolean naturalGas;
    private final boolean warmth;
    private final boolean eMobility;
    private final boolean service;
    private final String bankDescription;
    private final String bankId;
    private final boolean eBilling;
    private final boolean windAccount;
    private final boolean isActive;
    private final boolean electricityFeeder;
    private final Tariff[] tariffs;

    public ContractAccount(String contractAccountNr, String description, boolean electricity, boolean naturalGas,
            boolean warmth, boolean eMobility, boolean service, String bankDescription, String bankId, boolean eBilling,
            Tariff[] tariffs, boolean windAccount, boolean isActive, boolean electricityFeeder) {
        this.contractAccountNr = contractAccountNr;
        this.description = description;
        this.electricity = electricity;
        this.naturalGas = naturalGas;
        this.warmth = warmth;
        this.eMobility = eMobility;
        this.service = service;
        this.bankDescription = bankDescription;
        this.bankId = bankId;
        this.eBilling = eBilling;
        this.tariffs = tariffs;
        this.windAccount = windAccount;
        this.isActive = isActive;
        this.electricityFeeder = electricityFeeder;
    }

    public String getContractAccountNr() {
        return contractAccountNr;
    }

    public String getDescription() {
        return description;
    }

    public boolean isElectricity() {
        return electricity;
    }

    public boolean isNaturalGas() {
        return naturalGas;
    }

    public boolean isWarmth() {
        return warmth;
    }

    public boolean isEMobility() {
        return eMobility;
    }

    public boolean isService() {
        return service;
    }

    public String getBankDescription() {
        return bankDescription;
    }

    public String getBankId() {
        return bankId;
    }

    public boolean isEBilling() {
        return eBilling;
    }

    public boolean isWindAccount() {
        return windAccount;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isElectricityFeeder() {
        return electricityFeeder;
    }

    public Tariff[] getTariffs() {
        return tariffs;
    }
}
