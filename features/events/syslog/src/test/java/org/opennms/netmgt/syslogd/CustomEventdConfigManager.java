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
package org.opennms.netmgt.syslogd;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.config.EventdConfigManager;

/**
 * Customizable {@link EventdConfigManager} for test purposes.
 * It allows to override the default configuration by providing a map with custom values.
 * If now value is provided, the one from {@link EventdConfigManager} is used.
 */
public class CustomEventdConfigManager extends EventdConfigManager {
    private final Map<String, Object> properties;

    public CustomEventdConfigManager(final Map<String, Object> properties) throws IOException {
        this.properties = Objects.requireNonNull(properties);
    }

    @Override
    public String getTCPIpAddress() {
        return getValue("tcpIpAddress", super.getTCPIpAddress());
    }

    @Override
    public int getTCPPort() {
        return getValue("tcpPort", super.getTCPPort());
    }

    @Override
    public String getUDPIpAddress() {
        return getValue("udpIpAddress", super.getUDPIpAddress());
    }

    @Override
    public int getUDPPort() {
        return getValue("udpPort", super.getUDPPort());
    }

    private <T> T getValue(String key, T defaultValue) {
        final T value = (T) properties.getOrDefault(key, defaultValue);
        return value;
    }
}
