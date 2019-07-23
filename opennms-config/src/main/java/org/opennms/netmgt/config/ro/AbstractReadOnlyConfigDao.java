/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.ro;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.opennms.netmgt.config.ReadOnlyConfig;
import org.opennms.netmgt.config.threshd.Group;
import org.opennms.netmgt.dao.api.EffectiveConfigurationDao;
import org.opennms.netmgt.model.EffectiveConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

public abstract class AbstractReadOnlyConfigDao<T extends ReadOnlyConfig> implements ReadOnlyConfigDao<T> {

    protected static final long DEFAULT_CACHE_MILLIS = 300000; // 5 minutes

    private Map<String, Group> groupMap;

    private EffectiveConfigurationDao effectiveConfigurationDao;

    // A cached view of the Configuration
    private T cached;

    // Time in millis when Configuration was last read from DB and Cached
    protected long cachedAt;

    // Time the config was last written to the DB.
    private long configWriteTime;

    // hashcode of cached configuration.
    private int cachedHashCode;

    @Override
    public T getByKey(Class<T> type, String key, long cacheLengthInMillis) {
        if (cacheIsValid(cacheLengthInMillis)) {
            return cached;
        }
        EffectiveConfiguration config = effectiveConfigurationDao.getByKey(key);
        if (config == null) {
            return null;
        }
        if (config.getHashCode() == cachedHashCode) {
            // config hasn't changed
            return cached;
        }
        // Cache the retrieved value before returning it.
        configWriteTime = config.getLastUpdated().getTime();
        cachedHashCode = config.getHashCode();
        cached = unMarshallConfig(type, config.getConfiguration());
        cachedAt = System.currentTimeMillis();
        return cached;
    }

    @Override
    public Date getLastUpdated(String key) {
        return effectiveConfigurationDao.getLastUpdated(key);
    }

    public void setEffectiveConfigurationDao(EffectiveConfigurationDao effectiveConfigurationDao) {
        this.effectiveConfigurationDao = effectiveConfigurationDao;
    }

    private boolean cacheIsValid(long cacheLengthInMillis) {
        return System.currentTimeMillis() - cachedAt < cacheLengthInMillis;
    }

    private T unMarshallConfig(Class<T> type, String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector(objectMapper.getTypeFactory()));
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void reload() {
        // simply set cachedAt to zero and next get() will retrieve latest version
        cachedAt = 0;
    }

    protected Group getGroup(String groupName) {
        Group group = groupMap.get(groupName);
        if (group == null) {
            throw new IllegalArgumentException("Thresholding group " + groupName + " does not exist.");
        }
        return group;
    }


}
