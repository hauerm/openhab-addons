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
package org.openhab.binding.burgenlandenergie.internal.utils;

import com.google.gson.Gson;

/**
 * Helper to switch between development and production environment.
 *
 * @author Michael Hauer - Initial contribution
 */
public class GsonSingleton {
    public static Gson INSTANCE = new Gson();

    public static void destroy() {
        INSTANCE = null;
    }
}
