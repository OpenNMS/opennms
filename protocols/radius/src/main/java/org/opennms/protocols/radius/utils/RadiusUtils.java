/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
        // update 20200702:jcifs-ng uses bcprov instead of maintaining its own
        // implementations and provides the Crypto utility for easy retrieval
        Security.addProvider(new Provider("MD4", 0.0D, "MD4 for Radius") {
            {
                this.put("MessageDigest.MD4", (jcifs.util.Crypto.getMD4()).getClass().getName());
            }
        });
    }
}
