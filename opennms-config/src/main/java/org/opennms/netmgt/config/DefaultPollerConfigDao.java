/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * <p>DefaultPollerConfigDao class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class DefaultPollerConfigDao implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultPollerConfigDao.class);
    private Resource m_configResource;
    private String m_localServer;
    private Boolean m_verifyServer;
    
    private PollerConfig m_pollerConfig;
    
    /**
     * <p>Constructor for DefaultPollerConfigDao.</p>
     */
    public DefaultPollerConfigDao() {
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
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
            LOG.info("Resource '{}' does not seem to have an underlying File object; using input stream", getConfigResource());
        }
        
        try {
            if (file != null) {
                lastModified = file.lastModified();
                stream = new FileInputStream(file);
                LOG.debug("loadConfig: creating new PollerConfigFactory from file path: {}", file.getPath());
            } else {
                lastModified = System.currentTimeMillis();
                stream = getConfigResource().getInputStream();
                LOG.debug("loadConfig: creating new PollerConfigFactory from input stream");
            }

            setPollerConfig(new PollerConfigFactory(lastModified, stream, getLocalServer(), isVerifyServer()));
        } finally {
            if (stream != null) stream.close();
        }
    }
    
    /**
     * <p>getPollerConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.PollerConfig} object.
     */
    public PollerConfig getPollerConfig() {
        return m_pollerConfig;
    }

    private void setPollerConfig(PollerConfig pollerConfig) {
        m_pollerConfig = pollerConfig;
    }

    /**
     * <p>getConfigResource</p>
     *
     * @return a {@link org.springframework.core.io.Resource} object.
     */
    public Resource getConfigResource() {
        return m_configResource;
    }

    /**
     * <p>setConfigResource</p>
     *
     * @param configResource a {@link org.springframework.core.io.Resource} object.
     */
    public void setConfigResource(Resource configResource) {
        m_configResource = configResource;
    }

    /**
     * <p>getLocalServer</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLocalServer() {
        return m_localServer;
    }

    /**
     * <p>setLocalServer</p>
     *
     * @param localServer a {@link java.lang.String} object.
     */
    public void setLocalServer(String localServer) {
        m_localServer = localServer;
    }

    /**
     * <p>isVerifyServer</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean isVerifyServer() {
        return m_verifyServer;
    }

    /**
     * <p>setVerifyServer</p>
     *
     * @param verifyServer a {@link java.lang.Boolean} object.
     */
    public void setVerifyServer(Boolean verifyServer) {
        m_verifyServer = verifyServer;
    }
    
    
}
