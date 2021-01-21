/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.configservice;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.distributed.kvstore.api.BlobStore;


/**
 * We maintain and serve configurations from a centralized place in a uniform way.
 */
public class ConfigurationService {

    private final String STORE_CONTEXT = "config";

    private final BlobStore store;

    private final Map<String, List<ConfigurationChangeListener>> listeners = new ConcurrentHashMap<>();

    public ConfigurationService(final BlobStore store) {
        Objects.requireNonNull(store);
        this.store = store;
    }

    /**
     * Loads the latest available configuration specified by the URI and transforms it into the given JaxB class.
     */
    public <T> Optional<T> getConfigurationAsJaxb(final String uri, final Class<T> clazz) {
        Objects.requireNonNull(uri, "Uri cannot be null");
        Objects.requireNonNull(clazz, "Jaxb class cannot be null");
        Optional<String> config = getConfigurationAsString(uri);
        return config.map(c -> JaxbUtils.unmarshal(clazz, new StringReader(c)));
    }

    /**
     * Loads the latest available configuration specified by the URI as a raw String. It makes no assumptions about it's
     * format, e.g. XML, JSON or Properties
     */
    public Optional<String> getConfigurationAsString(final String uri) {
        Objects.requireNonNull(uri);
        Optional<byte[]> config = this.store.get(uri, STORE_CONTEXT);
        return config.map(c -> new String(c, StandardCharsets.UTF_8));
    }

    public void putConfiguration(final String uri, final String config) {
        Objects.requireNonNull(uri);
        Objects.requireNonNull(config);
        Optional<String> oldVersion = getConfigurationAsString(uri);
        this.store.put(uri, config.getBytes(StandardCharsets.UTF_8), STORE_CONTEXT);

        if (!config.equals(oldVersion.orElse(null))) {
            // config has actually changed => let's inform the listeners
            for (ConfigurationChangeListener listener : listeners.getOrDefault(uri, Collections.emptyList())) {
                listener.configurationHasChanged(uri);
            }
        }

    }

    public synchronized void registerForUpdates(final String uriOfConfig, final ConfigurationChangeListener listener) {
        listeners.computeIfAbsent(uriOfConfig, s -> new CopyOnWriteArrayList<>()).add(listener);
    }
}
