/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.asterisk.agi;

import org.asteriskjava.fastagi.AgiServerThread;
import org.asteriskjava.fastagi.ClassNameMappingStrategy;
import org.asteriskjava.fastagi.DefaultAgiServer;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;

/**
 * <p>AsteriskGateway class.</p>
 *
 * @author <A HREF="mailto:jeffg@opennms.org">Jeff Gehlbach</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @author <A HREF="mailto:jeffg@opennms.org">Jeff Gehlbach</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @version $Id: $
 */
public class AsteriskGateway extends AbstractServiceDaemon {

    AgiServerThread m_agiServerThread;
    private int m_port = 4573;
    private int m_maxPoolSize = 10;
    
    /**
     * <p>Constructor for AsteriskGateway.</p>
     */
    protected AsteriskGateway() {
        super("asterisk-gateway");
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        int port = Integer.getInteger("org.opennms.netmgt.asterisk.agi.listenPort", m_port);
        int maxPoolSize = Integer.getInteger("org.opennms.netmgt.asterisk.agi.maxPoolSize", m_maxPoolSize);
        
        DefaultAgiServer agiServer = new DefaultAgiServer(new ClassNameMappingStrategy(false));
        
        agiServer.setPort(port);
        agiServer.setMaximumPoolSize(maxPoolSize);
        
        m_agiServerThread = new AgiServerThread(agiServer);
        
        // This is the default, but be explicit
        m_agiServerThread.setDaemon(true);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void onStart() {
        m_agiServerThread.startup();
    }
    
    /** {@inheritDoc} */
    @Override
    protected void onStop() {
        m_agiServerThread.shutdown();
    }

}
