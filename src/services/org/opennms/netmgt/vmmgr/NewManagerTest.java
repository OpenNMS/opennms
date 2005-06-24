//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.vmmgr;

import java.util.Iterator;
import java.util.List;

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.opennms.netmgt.mock.MockUtil;

public class NewManagerTest extends TestCase {

    MBeanServer m_server = null;
    ObjectName m_objName = null;
    
    protected void setUp() {
        MockUtil.setupLogging();
    }
    
    public void testStart() throws Exception {
        NewManager mgr = new NewManager();
        mgr.doMain(new String[] { "start" });
        
        assertMBeanServerWithDomain("OpenNMS");
        assertMBean("OpenNMS:Name=FastExit");
        assertMBeanOperation("status");
        assertMBeanOperation("stop");
        assertMBean("MX4J:Name=HttpAdaptor");
        assertMBeanOperation("start");
        
        //invokeMBeanOperation("start");
        
        Thread.sleep(100000);
        
    }

    private void invokeMBeanOperation(String op) {
        assertNotNull("No active mbeanServer", m_server);
        assertNotNull("No current mbean", m_objName);
        try {
            m_server.invoke(m_objName, op, new Object[0], new String[0]);
        } catch (Exception e) {
            fail("Unable to invoke operation "+op+" on bean "+m_objName);
        }
    }

    private void assertMBeanOperation(String operation) {
        assertNotNull("No active mbeanServer", m_server);
        assertNotNull("No current mbean", m_objName);
        try {
            MBeanInfo info = m_server.getMBeanInfo(m_objName);
            assertNotNull(info);
            MBeanOperationInfo[] opInfos = info.getOperations();
            for (int i = 0; i < opInfos.length; i++) {
                MBeanOperationInfo opInfo = opInfos[i];
                if (operation.equals(opInfo.getName())) {
                    return;
                }
            }
            fail("Unable to find operation with name ["+operation+"] on mbean "+m_objName);
        } catch (Exception e) {
            fail("Unable to locate operation ["+operation+"] due to exception "+e.getMessage());
        }
    }

    private void assertMBean(String name) {
        assertNotNull("No active mbeanServer", m_server);
        try {
            m_objName = new ObjectName(name);
            assertTrue("Unable to find mbean registered with name ["+name+']', m_server.isRegistered(m_objName));
        } catch (MalformedObjectNameException e) {
            fail("Invalid Object Name "+name+": "+e.getMessage());
        }
    }

    private void assertMBeanServerWithDomain(String defaultDomain) {
        m_server = getMBeanServer(defaultDomain);
        assertNotNull("Unabled to located MBeanServer with default domain "+defaultDomain, m_server);
    }

    private MBeanServer getMBeanServer(String defaultDomain) {
        List servers = MBeanServerFactory.findMBeanServer(null);
        for (Iterator it = servers.iterator(); it.hasNext();) {
            MBeanServer server = (MBeanServer) it.next();
            if (defaultDomain == null || defaultDomain.equals(server.getDefaultDomain())) {
                return server;
            }
        }
        return null;
    }
    
    

}
