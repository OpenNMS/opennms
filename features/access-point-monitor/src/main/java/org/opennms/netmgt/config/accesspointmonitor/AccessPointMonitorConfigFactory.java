/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.accesspointmonitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * AccessPointMonitorConfigFactory class.
 * </p>
 * 
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 */
public class AccessPointMonitorConfigFactory {
	private static final Logger LOG = LoggerFactory.getLogger(AccessPointMonitorConfigFactory.class);

    private static final String ACCESS_POINT_MONITOR_CONFIG_FILE_NAME = "access-point-monitor-configuration.xml";

    /**
     * The singleton instance of this factory
     */
    private static AccessPointMonitorConfigFactory m_singleton = null;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * Loaded version
     */
    private long m_currentVersion = -1L;

    private AccessPointMonitorConfig m_accessPointMonitorConfig = null;

    public static AccessPointMonitorConfigFactory getInstance() {
        if (!m_loaded) {
            throw new IllegalStateException("The factory has not been initialized");
        }
        return m_singleton;
    }

    public static synchronized void setInstance(AccessPointMonitorConfigFactory instance) {
        m_singleton = instance;
        m_loaded = true;
    }

    public static synchronized void init() throws IOException, JAXBException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        File cfgFile = ConfigFileConstants.getConfigFileByName(ACCESS_POINT_MONITOR_CONFIG_FILE_NAME);
        LOG.debug("init: config file path: {}", cfgFile.getPath());

        InputStream is = null;
        try {
            is = new FileInputStream(cfgFile);
            setInstance(new AccessPointMonitorConfigFactory(cfgFile.lastModified(), is));
        } finally {
            if (is != null) {
                IOUtils.closeQuietly(is);
            }
        }
    }

    public static synchronized void reload() throws IOException, JAXBException {
        init();
        getInstance().update();
    }

    public synchronized void update() throws IOException, JAXBException {
        File cfgFile = ConfigFileConstants.getConfigFileByName(ACCESS_POINT_MONITOR_CONFIG_FILE_NAME);
        if (cfgFile.lastModified() > m_currentVersion) {
            m_currentVersion = cfgFile.lastModified();
            LOG.debug("init: config file path: {}", cfgFile.getPath());
            InputStream is = null;
            try {
                is = new FileInputStream(cfgFile);
                m_accessPointMonitorConfig = unmarshall(is);
            } finally {
                if (is != null) {
                    IOUtils.closeQuietly(is);
                }
            }
            LOG.debug("init: finished loading config file: {}", cfgFile.getPath());
        }
    }

    public AccessPointMonitorConfigFactory(long currentVersion, InputStream is) throws JAXBException {
        m_accessPointMonitorConfig = unmarshall(is);
        m_currentVersion = currentVersion;
    }

    private static AccessPointMonitorConfig unmarshall(InputStream is) throws JAXBException {
        InputStream apmcStream = is;
        JAXBContext context = JAXBContext.newInstance(AccessPointMonitorConfig.class);
        Unmarshaller um = context.createUnmarshaller();
        um.setSchema(null);
        return (AccessPointMonitorConfig) um.unmarshal(apmcStream);
    }

    public AccessPointMonitorConfig getConfig() {
        return m_accessPointMonitorConfig;
    }

    public static AccessPointMonitorConfig getConfigFromInstance() {
        return getInstance().getConfig();
    }

    public static String getDefaultConfigFilename() {
        return ACCESS_POINT_MONITOR_CONFIG_FILE_NAME;
    }
}
