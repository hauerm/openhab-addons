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
package org.openhab.binding.burgenlandenergie.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BurgenlandEnergieBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Hauer - Initial contribution
 */
@NonNullByDefault
public class BurgenlandEnergieBindingConstants {

    private static final String BINDING_ID = "burgenlandenergie";

    // List of all Thing Type UIDs

    // The sales api thing (may add grid-api thing in the future)
    public static final ThingTypeUID THING_SALES_API = new ThingTypeUID(BINDING_ID, "sales-api");

    // List of all Channel ids
    public static final String CURRENT_TARIFF_NAME = "current-tariff-name";
    public static final String CURRENT_TARIFF_PRICE_KWH = "current-tariff-price-kwh";
    public static final String CURRENT_TARIFF_PRICE_BASE = "current-tariff-price-base";
}
