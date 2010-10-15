/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: Aug 26, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
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
public class XmpPeerFactory extends PeerFactory {

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
    
    private XmpPeerFactory() throws MarshalException, ValidationException, FileNotFoundException, IOException {
        super();
    }
    
    
    /**
     * Initialize this factory
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     */
    public static synchronized void init() throws MarshalException, ValidationException, FileNotFoundException, IOException {
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
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws java.io.IOException if any.
     */
    public static synchronized void reload() throws MarshalException, ValidationException, FileNotFoundException, IOException {
        m_singleton = null;
        m_loaded = false;
        
        XmpConfigFactory.init();
        init();
    }
    
    /**
     * <p>getInstance</p>
     *
     * @return a {@link org.opennms.netmgt.config.XmpPeerFactory} object.
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
     * @param instance a {@link org.opennms.netmgt.config.XmpPeerFactory} object.
     */
    public static synchronized void setInstance(XmpPeerFactory instance) {
        m_singleton = instance;
        m_loaded = true;
    }
    
    /**
     * <p>getAgentConfig</p>
     *
     * @param agentInetAddress a {@link java.net.InetAddress} object.
     * @return a {@link org.opennms.netmgt.config.XmpAgentConfig} object.
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
