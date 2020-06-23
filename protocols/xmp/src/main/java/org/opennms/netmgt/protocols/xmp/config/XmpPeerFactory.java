/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.protocols.xmp.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;

import org.opennms.netmgt.config.xmpConfig.XmpConfig;

/**
 * <p>XmpPeerFactory class.</p>
 *
 * @author <A HREF="jeffg@opennms.org">Jeff Gehlbach</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @author <A HREF="jeffg@opennms.org">Jeff Gehlbach</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @version $Id: $
 */
public class XmpPeerFactory {

    /**
     * The singleton instance of this factory
     */
    private static XmpPeerFactory m_singleton;
    
    /**
     * Currently we're not backed up by a complex config file, we'll just
     * go to the XmpConfigFactory and always return the same peer config
     * regardless of the peer's IP address.  At some point this will
     * probably change, which is why this seemingly pointless factory
     * exists at all.
     */
    private static XmpConfig m_config;
    
    /**
     * Set to true if our configuration has been loaded 
     */
    private static boolean m_loaded = false;

    private XmpPeerFactory() throws FileNotFoundException, IOException {
        super();
    }

    /**
     * Initialize this factory
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     */
    public static synchronized void init() throws FileNotFoundException, IOException {
        if (m_loaded) {
            return;
        }
        
        // Someday we might do something substantial here
        XmpConfigFactory.init();
        m_config = XmpConfigFactory.getInstance().getXmpConfig();
        m_singleton = new XmpPeerFactory();
        m_loaded = true;
    }
    
    /**
     * <p>reload</p>
     *
     * @throws java.io.FileNotFoundException if any.
     * @throws java.io.IOException if any.
     */
    public static synchronized void reload() throws FileNotFoundException, IOException {
        m_singleton = null;
        m_loaded = false;
        
        XmpConfigFactory.init();
        init();
    }

    /**
     * <p>getInstance</p>
     *
     * @return a {@link org.opennms.netmgt.protocols.xmp.config.XmpPeerFactory} object.
     */
    public static synchronized XmpPeerFactory getInstance() {
        if (! m_loaded) {
            throw new IllegalStateException("The factory has not been initialized");
        }
        
        return m_singleton;
    }
    
    /**
     * <p>setInstance</p>
     *
     * @param instance a {@link org.opennms.netmgt.protocols.xmp.config.XmpPeerFactory} object.
     */
    public static synchronized void setInstance(XmpPeerFactory instance) {
        m_singleton = instance;
        m_loaded = true;
    }
    
    /**
     * <p>getAgentConfig</p>
     *
     * @param agentInetAddress a {@link java.net.InetAddress} object.
     * @return a {@link org.opennms.netmgt.protocols.xmp.config.XmpAgentConfig} object.
     */
    public synchronized XmpAgentConfig getAgentConfig(InetAddress agentInetAddress) {
        
        XmpAgentConfig config = new XmpAgentConfig();
        config.setAuthenUser(m_config.getAuthenUser());
        config.setPort(m_config.getPort());
        config.setRetry(m_config.getRetry());
        config.setTimeout(m_config.getTimeout());        
        return config;
    }
}
