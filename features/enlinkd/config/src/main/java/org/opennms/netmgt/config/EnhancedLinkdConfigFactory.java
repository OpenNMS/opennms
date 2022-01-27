/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import org.opennms.netmgt.config.enlinkd.EnlinkdConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * enhanced linkd service from the enlinkd-configuration xml file.
 *
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class EnhancedLinkdConfigFactory extends EnhancedLinkdConfigManager {
    private static final Logger LOG = LoggerFactory.getLogger(EnhancedLinkdConfigFactory.class);
    private static final String CONFIG_NAME = "enlinkd";

    public EnhancedLinkdConfigFactory() {
        // move to postConstruct to prevent dao bean not ready
    }

    /**
     * <p>Constructor for LinkdConfigFactory.</p>
     *
     * @param config EnlinkdConfiguration.
     */
    public EnhancedLinkdConfigFactory(EnlinkdConfiguration config) {
        this.config = config;
    }

    @PostConstruct
    public void postConstruct() {
        reload();
    }

    /**
     * <p>reload</p>
     *
     */
    @Override
    public void reload() {
        getReadLock().lock();
        this.config = this.loadConfig(this.getDefaultConfigId());
        getReadLock().unlock();
    }

    /**
     * Saves the current in-memory configuration to disk
     *
     * @throws java.io.IOException if any.
     */
    public void save() throws IOException {
        getWriteLock().lock();
        try {
            this.updateConfig(config);
        } finally {
            getWriteLock().unlock();
        }
    }

    @Override
    public String getConfigName() {
        return CONFIG_NAME;
    }
}
