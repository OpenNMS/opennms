//
//This file is part of the OpenNMS(R) Application.
//
//OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.
//
//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.                                                            
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//  
//For more information contact: 
// OpenNMS Licensing       <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//

package org.opennms.protocols.jmx.connectors;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;

/*
 * The JBossConnectionWrapper class manages the connection to the JBoss server.  The 
 * JBossConnectionFactory creates the connection to the server and closes the 
 * connection to the naming server, so the close() method doesn't need to do anything.
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
/**
 * <p>JBossConnectionWrapper class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class JBossConnectionWrapper implements ConnectionWrapper {
    private MBeanServer mbeanServer;
    
    /**
     * <p>Constructor for JBossConnectionWrapper.</p>
     *
     * @param mbeanServer a {@link javax.management.MBeanServer} object.
     */
    public JBossConnectionWrapper(MBeanServer mbeanServer) {
        this.mbeanServer = mbeanServer;
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.jmx.connectors.ConnectionWrapper#closeConnection()
     */
    /**
     * <p>close</p>
     */
    public void close() {
        mbeanServer = null;
    }
    
    /* (non-Javadoc)
     * @see org.opennms.protocols.jmx.connectors.ConnectionWrapper#getMBeanServer()
     */
    /**
     * <p>getMBeanServer</p>
     *
     * @return a {@link javax.management.MBeanServerConnection} object.
     */
    public MBeanServerConnection getMBeanServer() {
        return mbeanServer;
    }
}
