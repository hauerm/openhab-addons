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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.burgenlandenergie.internal.api.pojo.ContractAccount;

/**
 * The {@link IContractAccountListener} should be implemented by all things interested in {@link ContractAccount}
 * updates from {@link BEBridgeHandler}.
 *
 * @author Michael Hauer - Initial contribution
 */
@NonNullByDefault
public interface IContractAccountListener {
    void onContractAccountsUpdate(ContractAccount[] contractAccounts);
}
