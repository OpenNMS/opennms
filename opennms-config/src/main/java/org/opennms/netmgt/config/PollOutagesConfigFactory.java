/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import java.io.IOException;
import java.util.Date;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.config.api.PollOutagesConfigModifiable;
import org.opennms.netmgt.dao.api.EffectiveConfigurationDao;
import org.opennms.netmgt.model.EffectiveConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

/**
 * This is the singleton class used to load the configuration for the poller
 * outages from the poll-outages xml file. <strong>Note: </strong>Users of
 * this class should make sure the <em>init()</em> is called before calling
 * any other method to ensure the config is loaded before accessing other
 * convenience methods.
 * 
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class PollOutagesConfigFactory extends PollOutagesConfigManager implements PollOutagesConfigModifiable {
    /**
     * The singleton instance of this factory
     */
    private static PollOutagesConfigFactory m_singleton = null;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    @Autowired
    private EffectiveConfigurationDao effectiveConfigurationDao;

    /**
     * Private constructor
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     */
    PollOutagesConfigFactory(final String configFile) {
        setConfigResource(new FileSystemResource(configFile));
    }

    /**
     * Create a PollOutagesConfigFactory using the specified Spring resource.
     */
    public PollOutagesConfigFactory(final Resource resource) {
        setConfigResource(resource);
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @throws java.io.IOException
     *             if any.
     */
    public static synchronized void init() throws IOException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        PollOutagesConfigFactory factory = new PollOutagesConfigFactory(new FileSystemResource(ConfigFileConstants.getFile(ConfigFileConstants.POLL_OUTAGES_CONFIG_FILE_NAME)));
        factory.afterPropertiesSet();
        setInstance(factory);
    }

    @Override
    public void reload() throws IOException {
        m_loaded = false;
        init();
        getInstance().update();
    }

    /**
     * Return the singleton instance of this factory.
     * 
     * @return The current factory instance.
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static PollOutagesConfigFactory getInstance() {
        if (!m_loaded) {
            throw new IllegalStateException("The factory has not been initialized");
        }

        return m_singleton;
    }

    /**
     * <p>
     * setInstance
     * </p>
     * 
     * @param instance
     *            a {@link org.opennms.netmgt.config.PollOutagesConfigFactory}
     *            object.
     */
    public static void setInstance(final PollOutagesConfigFactory instance) {
        m_loaded = true;
        m_singleton = instance;
    }

    @Override
    public void saveCurrent() throws IOException {
        super.saveCurrent();
        saveEffective();
    }

    public void setEffectiveConfigurationDao(EffectiveConfigurationDao effectiveConfigurationDao) {
        this.effectiveConfigurationDao = effectiveConfigurationDao;
        saveEffective();
    }

    private void saveEffective() {
        EffectiveConfiguration entity = new EffectiveConfiguration();
        entity.setKey(ConfigFileConstants.getFileName(ConfigFileConstants.POLL_OUTAGES_CONFIG_FILE_NAME));
        entity.setConfiguration(getJsonConfig());
        entity.setLastUpdated(new Date());
        effectiveConfigurationDao.save(entity);
    }

    private String getJsonConfig() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector(objectMapper.getTypeFactory()));
            return objectMapper.writeValueAsString(getObject());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }
}
