/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.protocols.radius.utils;

import java.security.Provider;
import java.security.Security;

/**
 * @author jmk <jm+opennms@kubek.fr>
 *
 */
public class RadiusUtils {

    public final static boolean isEAPTTLS(final String authType) {
        return authType.equalsIgnoreCase("eap-ttls") ||authType.equalsIgnoreCase("eapttls");
    }
    public final static boolean isTunneling(final String authType) {
        return isEAPTTLS(authType) || authType.equalsIgnoreCase("peap");
    }

    public final static void loadSecurityProvider() {
        // This adds support for MD4 digest used by mschapv2 - NMS-9763
        Security.addProvider(new Provider("MD4", 0.0D, "MD4 for Radius") {
            {
                this.put("MessageDigest.MD4", jcifs.util.MD4.class.getName());
            }
        });
    }
}
