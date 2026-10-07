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
package org.opennms.web.rest.v2;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.events.EventBuilder;

/**
 * Helpers shared by the two threshold configuration REST services.
 */
abstract class ThresholdRestSupport {

    /** Matches the source string the JSP threshold editor used, so event history stays continuous. */
    private static final String EVENT_SOURCE = "Web UI";

    private static final String DAEMON_NAME = "Threshd";

    private ThresholdRestSupport() {
    }

    /**
     * Computes the entity tag of a DTO from its serialized form.
     *
     * <p>Derived from the content rather than a stored revision, because the configuration manager does not
     * version the documents it holds. That is enough for the purpose here: detecting that the thing the
     * client read is no longer the thing that is stored.</p>
     */
    static String etagOf(final ObjectMapper objectMapper, final Object dto) {
        try {
            final byte[] json = objectMapper.writeValueAsString(dto).getBytes(StandardCharsets.UTF_8);
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(json);

            final StringBuilder sb = new StringBuilder(hash.length * 2);
            for (final byte b : hash) {
                sb.append(Character.forDigit((b >> 4) & 0xf, 16));
                sb.append(Character.forDigit(b & 0xf, 16));
            }
            return sb.toString();
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required by the platform but not available.", e);
        } catch (final Exception e) {
            throw new IllegalStateException("Could not compute the entity tag of a threshold configuration.", e);
        }
    }

    /**
     * @return true when the client either sent no If-Match at all (and so accepts a last-write-wins update)
     *         or sent one that still matches
     */
    static boolean matchesIfMatch(final String ifMatch, final String etag) {
        if (ifMatch == null || ifMatch.trim().isEmpty()) {
            return true;
        }

        for (String candidate : ifMatch.split(",")) {
            candidate = candidate.trim();
            if ("*".equals(candidate)) {
                return true;
            }
            // Strip the weak-validator prefix and the quotes a well-behaved client sends them in.
            if (candidate.startsWith("W/")) {
                candidate = candidate.substring(2);
            }
            if (candidate.length() > 1 && candidate.startsWith("\"") && candidate.endsWith("\"")) {
                candidate = candidate.substring(1, candidate.length() - 1);
            }
            if (candidate.equals(etag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tells the threshd daemon to re-read one of its configuration files.
     *
     * <p>Distinct from the configuration manager's own update notification, which only keeps the DAOs in
     * this JVM current: without this event the daemon keeps running against the configuration it read at
     * startup.</p>
     */
    static void sendReloadEvent(final EventProxy eventProxy, final String configFileName) throws Exception {
        final EventBuilder builder = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, EVENT_SOURCE);
        builder.setHost(InetAddressUtils.getLocalHostName());
        builder.addParam(EventConstants.PARM_DAEMON_NAME, DAEMON_NAME);
        builder.addParam(EventConstants.PARM_CONFIG_FILE_NAME, configFileName);
        eventProxy.send(builder.getEvent());
    }
}
