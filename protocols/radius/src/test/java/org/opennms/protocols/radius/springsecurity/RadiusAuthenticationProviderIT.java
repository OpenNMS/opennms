/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.protocols.radius.springsecurity;

import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.tinyradius.util.RadiusServer;

import net.jradius.client.auth.CHAPAuthenticator;

public class RadiusAuthenticationProviderIT {

    private static final String SHARED_SECRET = "SECRET";
    private static final String USER = "user1";
    private static final String PASSWORD = "u5er1p455";
    private static final String PASSWORD_WRONG = "xxxx";

    private RadiusServer radiusServer = new RadiusServer() {
        @Override
        public String getSharedSecret(InetSocketAddress client) {
            return SHARED_SECRET;
        }

        @Override
        public String getUserPassword(String userName) {
            if (USER.equals(userName)) {
                return PASSWORD;
            }
            throw new IllegalArgumentException("No password for user " + userName + " defined");
        }
    };

    @Before
    public void setUp() {
        radiusServer.start(true, false);
        if (PASSWORD.equals(PASSWORD_WRONG)) {
            throw new IllegalStateException("PASSWORD and PASSWORD_WRONG cannot match");
        }
    }

    @After
    public void tearDown() {
        radiusServer.stop();
    }

    // Ensure that whatever the first response was, it is not re-used for another user
    // See NMS-10212
    @Test
    public void verifyAuthenticatorIsNotReused() {
        final RadiusAuthenticationProvider provider = new RadiusAuthenticationProvider("127.0.0.1", SHARED_SECRET);
        provider.setAuthTypeClass(CHAPAuthenticator.class); // The authenticator does not matter,
                                                            // the problem should occurs with all, except null

        // Verify that authenticating with an existing user works
        final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(USER, PASSWORD);
        provider.retrieveUser(USER, token);

        // Verify that authenticating without an existing user also works
        // This means, that the access should be denied. See NMS-10212
        try {
            provider.retrieveUser(USER, new UsernamePasswordAuthenticationToken(USER, PASSWORD_WRONG));
            Assert.fail("Expected an AuthenticationException but did not receive one. Failing..");
        } catch (AuthenticationException ex) {
            // expected Exception
        }
    }
}
