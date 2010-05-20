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
package org.opennms.netmgt.xmlrpcd.jmx;

import org.apache.xmlrpc.XmlRpc;
import org.opennms.core.fiber.Fiber;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Provisioner implements ProvisionerMBean {

    private ClassPathXmlApplicationContext m_context;
    int m_status = Fiber.START_PENDING;
    
    // used only for testing
    ApplicationContext getContext() {
        return m_context;
    }

    public void init() {
        XmlRpc.debug = "true".equalsIgnoreCase(System.getProperty("xmlrpc.debug", "false"));
    }

    public void start() {
        m_status = Fiber.STARTING;
        ThreadCategory.getInstance().debug("SPRING: thread.classLoader="+Thread.currentThread().getContextClassLoader());;
        m_context = BeanUtils.getFactory("provisionerContext", ClassPathXmlApplicationContext.class);
        ThreadCategory.getInstance().debug("SPRING: context.classLoader="+m_context.getClassLoader());
        m_status = Fiber.RUNNING;
    }

    public void stop() {
        m_status = Fiber.STOP_PENDING;
        m_context.close();
        
        
        m_status = Fiber.STOPPED;
    }

    /**
     * Returns the status of this Fiber.
     */
    public int getStatus() {
        return m_status;
    }

    public String getStatusText() {
        return Fiber.STATUS_NAMES[m_status];
    }

    public String status() {
        return getStatusText();
    }
}
