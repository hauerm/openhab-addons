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
package org.openhab.binding.burgenlandenergie.internal.api.jsonModels.request;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * POJO
 * 
 * @author Michael Hauer - Initial contribution
 */
@NonNullByDefault
public class ContractAccountBody {

    private final String active;
    private final String eMobility;
    private final String ega;
    private final String electricityFeeders;
    private final String naturalGas;
    private final String sd;
    private final String service;
    private final String warmth;

    public ContractAccountBody(String active, String eMobility, String ega, String electricityFeeders,
            String naturalGas, String sd, String service, String warmth) {
        this.active = active;
        this.eMobility = eMobility;
        this.ega = ega;
        this.electricityFeeders = electricityFeeders;
        this.naturalGas = naturalGas;
        this.sd = sd;
        this.service = service;
        this.warmth = warmth;
    }

    public String getActive() {
        return active;
    }

    public String getEMobility() {
        return eMobility;
    }

    public String getEga() {
        return ega;
    }

    public String getElectricityFeeders() {
        return electricityFeeders;
    }

    public String getNaturalGas() {
        return naturalGas;
    }

    public String getSd() {
        return sd;
    }

    public String getService() {
        return service;
    }

    public String getWarmth() {
        return warmth;
    }
}
