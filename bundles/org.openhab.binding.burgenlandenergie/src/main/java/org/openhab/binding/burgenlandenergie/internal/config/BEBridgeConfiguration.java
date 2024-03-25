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
 * The {@link BEBridgeConfiguration} class contains fields mapping bridge configuration parameters.
 *
 * @author Michael Hauer - Initial contribution
 */
@NonNullByDefault
public class BEBridgeConfiguration {

    // The meine.burgenlandenergie.at username
    public String username = "";

    // The meine.burgenlandenergie.at password
    public String password = "";

    public String customerNr = "";

    public int refreshIntervalMins = 30;
}
