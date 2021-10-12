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

import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.features.config.service.util.DefaultAbstractCmJaxbConfigDaoUpdateCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
    protected ConfigurationManagerService configurationManagerService;

    private Class<ENTITY_CLASS> entityClass;
    private String description;
    private ConcurrentHashMap<String, ENTITY_CLASS> lastKnownEntityMap = new ConcurrentHashMap<>();

    /**
     * ConfigName use for the Config and Schema
     *
     * @return ConfigName
     */
    abstract protected String getConfigName();

    /**
     * It will provide the default callback for all ConfigDao, override if you needed
     *
     * @return Consumer
     */
    Consumer<ConfigUpdateInfo> getUpdateCallback() {
        return new DefaultAbstractCmJaxbConfigDaoUpdateCallback<>(this);
    }

    /**
     * The default configId when getConfig without passing configId
     *
     * @return configId
     */
    abstract protected String getDefaultConfigId();

    /**
     * <p>Constructor for AbstractJaxbConfigDao.</p>
     * It will use {@link DefaultAbstractCmJaxbConfigDaoUpdateCallback},
     * override getUpdateCallback if you need to change.
     *
     * @param entityClass a {@link java.lang.Class} object.
     * @param description a {@link java.lang.String} object.
     * @see #getUpdateCallback()
     */
    public AbstractCmJaxbConfigDao(final Class<ENTITY_CLASS> entityClass, final String description) {
        this.entityClass = entityClass;
        this.description = description;
    }

    /**
     * Add default callback
     */
    @PostConstruct
    public void postConstruct() throws IOException {
        Set<String> configIds = configurationManagerService.getConfigIds(this.getConfigName());
        configIds.forEach(configId -> this.addOnReloadedCallback(configId, getUpdateCallback()));
    }

    /**
     * It will load the default config
     *
     * @return ConfigObject
     */
    public ENTITY_CLASS loadConfig() {
        return this.loadConfig(this.getDefaultConfigId());
    }

    /**
     * loadConfig from database by CM. If it is already in cache, it will update the cache.
     *
     * @param configId
     * @return ConfigObject
     */
    public ENTITY_CLASS loadConfig(final String configId) {
        long startTime = System.currentTimeMillis();

        LOG.debug("Loading {} configuration from {}", description, configId);
        Optional<ENTITY_CLASS> configOptional;

        try {
            configOptional = configurationManagerService
                    .getXmlConfiguration(this.getConfigName(), configId)
                    .map(s -> JaxbUtils.unmarshal(entityClass, s));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (configOptional.isEmpty()) {
            throw new RuntimeException("NOT_FOUND: configName=" + this.getConfigName() + " configId=" + configId);
        }
        final ENTITY_CLASS config = configOptional.get();
        long endTime = System.currentTimeMillis();
        LOG.info("Loaded {} in {} ms", getDescription(), (endTime - startTime));

        return lastKnownEntityMap.compute(configId, (k, v) -> {
            if (v != null) {
                BeanUtils.copyProperties(config, v);
                return v;
            } else {
                return config;
            }
        });
    }

    /**
     * It will the config in cache, if nothing found it will load from db.
     * <b>Please notice that, config can be different in db.</b>
     * @param configId
     * @return config
     */
    public ENTITY_CLASS getConfig(String configId){
        // cannot use computeIfAbsent, it will cause IllegalStateException
        ENTITY_CLASS config = lastKnownEntityMap.get(configId);
        if (config == null){
            return this.loadConfig(configId);
        }
        return lastKnownEntityMap.computeIfAbsent(configId, this::loadConfig);
    }

    public void updateConfig(final String configId, String configStr) throws IOException {
        configurationManagerService.updateConfiguration(this.getConfigName(), configId, new JsonAsString(configStr));
    }

    /**
     * it will update the default config
     *
     * @param configStr
     * @throws IOException
     * @see #updateConfig(String, String)
     */
    public void updateConfig(String configStr) throws IOException {
        this.updateConfig(this.getDefaultConfigId(), configStr);
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
    public void addOnReloadedCallback(String configId, Consumer<ConfigUpdateInfo> callback) {
        Objects.requireNonNull(callback);
        configurationManagerService.registerReloadConsumer(new ConfigUpdateInfo(this.getConfigName(), configId), callback);
    }
}
