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
package org.openhab.binding.burgenlandenergie.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TariffThingConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Michael Hauer - Initial contribution
 */
@NonNullByDefault
public class TariffThingConfiguration {

    // The meine.burgenlandenergie.at username
    public String username = "";

    // The meine.burgenlandenergie.at password
    public String password = "";
    public String customerNr = "";

    // The sales contract account number
    public String contractAccountNr = "";

    // The division of the contract account ("E", "G")
    public String division = "";
}
