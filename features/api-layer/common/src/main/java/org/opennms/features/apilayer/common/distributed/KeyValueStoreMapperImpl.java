/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.common.distributed;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.opennms.integration.api.v1.distributed.KeyValueStore;
import org.opennms.integration.api.v1.distributed.KeyValueStoreMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyValueStoreMapperImpl  implements KeyValueStoreMapper {
    private static final Logger LOG = LoggerFactory.getLogger(KeyValueStoreMapperImpl.class);
    private final Map<String, KeyValueStore> keyValueStoreMap = new ConcurrentHashMap<>();

    public void onBind(org.opennms.features.distributed.kvstore.api.KeyValueStore store, Map properties) {
        LOG.debug("Key-value store mapper bind called with {}: {}", store, properties);
        if(store != null) {
            keyValueStoreMap.put(store.getName(), new KeyValueStoreWrapper<>(store));
        }
    }

    public void onUnbind(org.opennms.features.distributed.kvstore.api.KeyValueStore store, Map properties) {
        LOG.debug("Key-value store mapper unBind called with {}: {}", store, properties);
        if(store != null) {
            keyValueStoreMap.remove(store.getName());
        }
    }

    @Override
    public Set<String> getStoreNames() {
        return keyValueStoreMap.keySet();
    }

    @Override
    public KeyValueStore getKeyValueStoreByName(String name) {
        return keyValueStoreMap.get(name);
    }
}
