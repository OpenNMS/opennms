/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.features.config.service.util;

import org.opennms.features.config.exception.ValidationException;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.opennms.features.config.service.api.EventType.UPDATE;
import static org.opennms.features.config.service.util.PropertiesConversionUtil.mapToJsonString;

/**
 * Wrapper around CM to read and write properties.
 * Derived from org.opennms.core.utils.PropertiesCache.
 * It operates on cm instead of a file.
 */
public class CmProperties {

    private Map<String, Object> properties;
    private final ConfigurationManagerService cm;
    private final ConfigUpdateInfo configIdentifier;
    private final Lock lock = new ReentrantLock();
    private boolean needReload = false;

    public CmProperties(final ConfigurationManagerService cm, ConfigUpdateInfo configIdentifier) {
        this.cm = Objects.requireNonNull(cm);
        this.configIdentifier = Objects.requireNonNull(configIdentifier);
        cm.registerEventHandler(UPDATE, configIdentifier, (ConfigUpdateInfo key) -> this.needReload = true);
    }

    private Optional<Map<String, Object>> read() {
        Optional<Map<String, Object>> result;
        result = this.cm.getJSONStrConfiguration(configIdentifier.getConfigName(), configIdentifier.getConfigId())
                .map(PropertiesConversionUtil::jsonToMap);
        needReload = false;
        return result;
    }

    private void write() throws ValidationException {
        Map<String, Object> entries = new HashMap<>();
        for (Map.Entry<?, ?> entry : this.properties.entrySet()) {
            entries.put(entry.getKey().toString(), entry.getValue());
        }
        this.cm.updateConfiguration(this.configIdentifier.getConfigName(), this.configIdentifier.getConfigId(), mapToJsonString(entries), false);
    }

    public Map<String, Object> get() {
        lock.lock();
        try {
            if (properties == null || needReload) {
                this.properties = read().orElse(new ConcurrentHashMap<>());
            }
            return properties;
        } finally {
            lock.unlock();
        }
    }

    public void setProperty(final String key, final Object value) throws ValidationException {
        lock.lock();
        try {
            if (!value.equals(get().get(key))) {
                get().put(key, value);
                write();
            }
        } finally {
            lock.unlock();
        }
    }

    public Object getProperty(final String key) {
        lock.lock();
        try {
            return get().get(key);
        } finally {
            lock.unlock();
        }
    }
}
