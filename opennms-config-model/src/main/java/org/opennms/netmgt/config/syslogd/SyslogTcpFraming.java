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
 * The message framings defined by RFC 6587 for syslog over TCP.
 */
public enum SyslogTcpFraming {

    /**
     * Detect the framing from the first frame of each connection, then keep it for the
     * life of that connection.
     */
    AUTO("auto"),

    /**
     * RFC 6587 section 3.4.1: {@code MSG-LEN SP SYSLOG-MSG}.
     */
    OCTET_COUNTING("octet-counting"),

    /**
     * RFC 6587 section 3.4.2: messages separated by a trailer, in practice LF.
     */
    NON_TRANSPARENT("non-transparent");

    private final String configValue;

    SyslogTcpFraming(final String configValue) {
        this.configValue = configValue;
    }

    public String getConfigValue() {
        return configValue;
    }

    /**
     * Accepts the hyphenated config spellings as well as the enum names, so that
     * both {@code non-transparent} and {@code NON_TRANSPARENT} work in
     * syslogd-configuration.xml and in the Minion .cfg.
     */
    public static SyslogTcpFraming fromConfigValue(final String value) {
        if (value == null || value.trim().isEmpty()) {
            return AUTO;
        }
        final String normalized = value.trim().toLowerCase().replace('_', '-');
        for (final SyslogTcpFraming framing : values()) {
            if (framing.configValue.equals(normalized)) {
                return framing;
            }
        }
        throw new IllegalArgumentException("Unsupported syslog TCP framing '" + value
                + "'. Supported values are: auto, octet-counting, non-transparent.");
    }

    @Override
    public String toString() {
        return configValue;
    }
}
