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
