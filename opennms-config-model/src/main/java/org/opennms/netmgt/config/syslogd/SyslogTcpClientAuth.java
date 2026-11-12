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
package org.opennms.netmgt.config.syslogd;

/**
 * How the syslog TCP listener treats client certificates when TLS is enabled.
 */
public enum SyslogTcpClientAuth {

    /** Do not ask the sender for a certificate. */
    NONE("none"),

    /** Ask for a certificate, but accept senders that do not present one. */
    OPTIONAL("optional"),

    /** Reject senders that do not present a trusted certificate. */
    REQUIRE("require");

    private final String configValue;

    SyslogTcpClientAuth(final String configValue) {
        this.configValue = configValue;
    }

    public String getConfigValue() {
        return configValue;
    }

    public static SyslogTcpClientAuth fromConfigValue(final String value) {
        if (value == null || value.trim().isEmpty()) {
            return NONE;
        }
        final String normalized = value.trim().toLowerCase();
        for (final SyslogTcpClientAuth clientAuth : values()) {
            if (clientAuth.configValue.equals(normalized)) {
                return clientAuth;
            }
        }
        throw new IllegalArgumentException("Unsupported syslog TCP client authentication mode '" + value
                + "'. Supported values are: none, optional, require.");
    }

    @Override
    public String toString() {
        return configValue;
    }
}
