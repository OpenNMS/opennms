/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.remote.support;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.opennms.netmgt.poller.remote.PollerSettings;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

/**
 * <p>DefaultPollerSettings class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class DefaultPollerSettings implements InitializingBean, PollerSettings {
    
    private final String MONITOR_ID_KEY = "locationMonitorId";
    
    private Resource m_configResource;
    
    private Properties m_settings;

    /**
     * <p>getMonitorId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Override
    public Integer getMonitorId() {
        String monIdStr = m_settings.getProperty(MONITOR_ID_KEY);
        return (monIdStr == null ? null : Integer.decode(monIdStr));
    }

    /** {@inheritDoc} */
    @Override
    public void setMonitorId(Integer monitorId) {
        if (monitorId == null)
            m_settings.remove(MONITOR_ID_KEY);
        else
            m_settings.setProperty(MONITOR_ID_KEY, monitorId.toString());
        
        save();
    }
    
    private void save() {
        FileOutputStream out = null;
        try {
            File configFile = m_configResource.getFile();
            if (!configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(configFile);
            m_settings.store(out, "Properties File for OpenNMS Remote Poller");
        } catch (IOException e) {
            throw new DataAccessResourceFailureException("Unable to save properties to "+m_configResource, e);
        } finally {
            if (out != null) {
                try { out.close(); } catch (IOException e) {};
            }
        }
    }

    /**
     * <p>setConfigurationResource</p>
     *
     * @param configResource a {@link org.springframework.core.io.Resource} object.
     */
    public void setConfigurationResource(Resource configResource) {
        m_configResource = configResource;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_configResource, "The configurationDir property must be set");
        
        if (m_configResource.exists()) {
            m_settings = PropertiesLoaderUtils.loadProperties(m_configResource);
        } else {
            m_settings = new Properties();
        }
        
    }
    
    

}
