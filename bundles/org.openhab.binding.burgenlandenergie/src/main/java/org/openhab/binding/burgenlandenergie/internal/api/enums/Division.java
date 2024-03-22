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
package org.openhab.binding.burgenlandenergie.internal.api.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Currently supported divisions.
 *
 * @author Michael Hauer - Initial contribution
 */
@NonNullByDefault
public enum Division {
    ELECTRICITY("10"),
    NATURALGAS("60");

    private final String division;

    Division(String division) {
        this.division = division;
    }

    public String getDivision() {
        return division;
    }
}
