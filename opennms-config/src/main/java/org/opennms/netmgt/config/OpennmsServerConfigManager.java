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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;

import org.apache.commons.io.IOUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.OpennmsServerConfig;
import org.opennms.netmgt.config.server.LocalServer;

/**
 * <p>OpennmsServerConfigManager class.</p>
 */
public class OpennmsServerConfigManager implements OpennmsServerConfig {
    /**
     * The config class loaded from the config file
     */
    private LocalServer m_config;
    
    /**
     * <p>Constructor for OpennmsServerConfigManager.</p>
     *
     * @param is a {@link java.io.InputStream} object.
     */
    protected OpennmsServerConfigManager(final InputStream is) {
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(is);
            m_config = JaxbUtils.unmarshal(LocalServer.class, isr);
        } finally {
            IOUtils.closeQuietly(isr);
        }
    }

    /**
     * Return the local opennms server name.
     *
     * @return the name of the local opennms server
     */
    public String getServerName() {
        return m_config.getServerName();
    }

    /**
     * Return the default critical path IP
     *
     * @return the default critical path IP
     */
    public InetAddress getDefaultCriticalPathIp() {
        return m_config.getDefaultCriticalPathIp().orElse(null);
    }

    /**
     * Return the default critical path service
     *
     * @return the default critical path service
     */
    public String getDefaultCriticalPathService() {
        return m_config.getDefaultCriticalPathService().orElse(null);
    }

    /**
     * Return the default critical path timeout
     *
     * @return the default critical path timeout
     */
    public int getDefaultCriticalPathTimeout() {
        return m_config.getDefaultCriticalPathTimeout();
    }

    /**
     * Return the default critical path retries
     *
     * @return the default critical path retries
     */
    public int getDefaultCriticalPathRetries() {
        return m_config.getDefaultCriticalPathRetries();
    }

    /**
     * Return the boolean flag verify server to determine if poller what to use
     * server to restrict services to poll.
     *
     * @return boolean flag
     */
    public boolean verifyServer() {
        return Boolean.valueOf(m_config.getVerifyServer());
    }

}
