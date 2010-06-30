/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: October 11, 2006
 *
 * Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
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
    public Integer getMonitorId() {
        String monIdStr = m_settings.getProperty(MONITOR_ID_KEY);
        return (monIdStr == null ? null : Integer.decode(monIdStr));
    }

    /** {@inheritDoc} */
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
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_configResource, "The configurationDir property must be set");
        
        if (m_configResource.exists()) {
            m_settings = PropertiesLoaderUtils.loadProperties(m_configResource);
        } else {
            m_settings = new Properties();
        }
        
    }
    
    

}
