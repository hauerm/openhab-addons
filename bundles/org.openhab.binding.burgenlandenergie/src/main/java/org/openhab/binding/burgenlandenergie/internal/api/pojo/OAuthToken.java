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

import java.time.Instant;

/**
 * POJO
 * 
 * @author Michael Hauer - Initial contribution
 */
public class OAuthToken {
    private final String idToken;
    private final String accessToken;
    private final String refreshToken;
    private final String expireIn;
    private final Instant expiresAt;

    public OAuthToken(String idToken, String accessToken, String refreshToken, String expireIn) {
        this.idToken = idToken;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expireIn = expireIn;
        // calculate expiresAt instant from given expireIn string
        this.expiresAt = Instant.now().plusSeconds(Long.parseLong(expireIn));
    }

    public String getIdToken() {
        return idToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getExpireIn() {
        return expireIn;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
