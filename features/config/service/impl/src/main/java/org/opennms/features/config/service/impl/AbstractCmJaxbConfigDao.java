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

package org.opennms.features.config.service.impl;

import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * <p>Abstract AbstractCmJaxbConfigDao class.</p>
 *
 * @param <ENTITY_CLASS> Configuration class
 * @version $Id: $
 */
public abstract class AbstractCmJaxbConfigDao<ENTITY_CLASS> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCmJaxbConfigDao.class);

    @Autowired
    private ConfigurationManagerService configurationManagerService;

    private Class<ENTITY_CLASS> entityClass;
    private String description;
    private final Collection<Consumer<ENTITY_CLASS>> onReloadCausedChangeCallbacks = new ArrayList<>();
    private ConcurrentHashMap<String, ENTITY_CLASS> lastKnownEntityMap = new ConcurrentHashMap<>();

    /**
     * ConfigName use for the Config and Schema
     *
     * @return ConfigName
     */
    abstract protected String getConfigName();


    /**
     * The default configId when getConfig without passing configId
     *
     * @return configId
     */
    abstract protected String getDefaultConfigId();

    /**
     * <p>Constructor for AbstractJaxbConfigDao.</p>
     *
     * @param entityClass a {@link java.lang.Class} object.
     * @param description a {@link java.lang.String} object.
     */
    public AbstractCmJaxbConfigDao(final Class<ENTITY_CLASS> entityClass, final String description) {
        super();
        this.entityClass = entityClass;
        this.description = description;
    }

    /**
     * loadConfig from CM. If the config is already stored in cache, it will also trigger reload callback
     *
     * @param configId
     * @return ConfigObject
     * @throws IOException
     */
    protected ENTITY_CLASS loadConfig(final String configId) {
        long startTime = System.currentTimeMillis();

        LOG.debug("Loading {} configuration from {}", description, configId);
        Optional<ENTITY_CLASS> configOptional = null;
        try {
            configOptional = configurationManagerService.getConfiguration(this.getConfigName(), configId, entityClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (configOptional.isEmpty()) {
            throw new RuntimeException("NOT_FOUND: configName=" + this.getConfigName() + " configId=" + configId);
        }
        ENTITY_CLASS config = configOptional.get();
        long endTime = System.currentTimeMillis();
        LOG.info("Loaded {} in {} ms", getDescription(), (endTime - startTime));

        // If the config is not load in the first time, we will trigger the callbacks for this change
        ENTITY_CLASS lastKnownEntity = lastKnownEntityMap.get(configId);
        if (lastKnownEntity != null) {
            synchronized (onReloadCausedChangeCallbacks) {
                if (!onReloadCausedChangeCallbacks.isEmpty()) {
                    LOG.debug("Calling onReloaded callbacks");
                    try {
                        //TODO: Freddy PE-13 reconsider the possiblility of exception during loop
                        onReloadCausedChangeCallbacks.forEach(c -> c.accept(config));
                    } catch (Exception e) {
                        LOG.warn("Encountered exception while calling onReloaded callbacks", e);
                    }
                }
            }
        }
        return config;
    }

    /**
     * get default config object
     *
     * @return config object
     */
    public ENTITY_CLASS getConfig(String configId) {
        ENTITY_CLASS config = lastKnownEntityMap.computeIfAbsent(configId, this::loadConfig);
        lastKnownEntityMap.put(configId, config);
        return config;
    }

    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param callback a callback that will be called when the entity maintained by this DAO is reloaded
     */
    public void addOnReloadedCallback(Consumer<ENTITY_CLASS> callback) {
        Objects.requireNonNull(callback);

        synchronized (onReloadCausedChangeCallbacks) {
            onReloadCausedChangeCallbacks.add(callback);
        }
    }
}
