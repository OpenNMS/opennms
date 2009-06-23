/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created April 4, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class DefaultPollerConfigDao implements InitializingBean {
    private Resource m_configResource;
    private String m_localServer;
    private Boolean m_verifyServer;
    
    private PollerConfig m_pollerConfig;
    
    public DefaultPollerConfigDao() {
    }

    public void afterPropertiesSet() throws Exception {
        Assert.state(m_configResource != null, "property configResource must be set to a non-null value");
        Assert.state(m_localServer != null, "property localServer must be set to a non-null value");
        Assert.state(m_verifyServer != null, "property verifyServer must be set to a non-null value");
        
        loadConfig();
    }

    private void loadConfig() throws Exception {
        InputStream stream = null;
        long lastModified;
        
        File file = null;
        try {
            file = getConfigResource().getFile();
        } catch (IOException e) {
            log().info("Resource '" + getConfigResource() + "' does not seem to have an underlying File object; using ");
        }
        
        if (file != null) {
            lastModified = file.lastModified();
            stream = new FileInputStream(file);
        } else {
            lastModified = System.currentTimeMillis();
            stream = getConfigResource().getInputStream();
        }

        setPollerConfig(new PollerConfigFactory(lastModified, stream, getLocalServer(), isVerifyServer()));
    }
    
    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public PollerConfig getPollerConfig() {
        return m_pollerConfig;
    }

    private void setPollerConfig(PollerConfig pollerConfig) {
        m_pollerConfig = pollerConfig;
    }

    public Resource getConfigResource() {
        return m_configResource;
    }

    public void setConfigResource(Resource configResource) {
        m_configResource = configResource;
    }

    public String getLocalServer() {
        return m_localServer;
    }

    public void setLocalServer(String localServer) {
        m_localServer = localServer;
    }

    public Boolean isVerifyServer() {
        return m_verifyServer;
    }

    public void setVerifyServer(Boolean verifyServer) {
        m_verifyServer = verifyServer;
    }
    
    
}
