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
public record Tariff(String installation, String division, String tariffType, String tariffName,
        boolean electricityWarmth, String tariffSegment, double workPrice, double basePrice, String boundUntil,
        String priceGuaranteeUntil) {
}
