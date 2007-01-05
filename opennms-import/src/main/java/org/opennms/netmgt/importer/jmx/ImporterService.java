//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.importer.jmx;

import org.opennms.core.fiber.Fiber;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.DefaultLocatorFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ImporterService implements ImporterServiceMBean {
	
	private static final String NAME = org.opennms.netmgt.importer.ImporterService.NAME;
	
    private ClassPathXmlApplicationContext m_context;
    int m_status = Fiber.START_PENDING;
    
    
    // used only for testing
    ApplicationContext getContext() {
        return m_context;
    }

    public void init() {
    	ThreadCategory.setPrefix(ImporterService.NAME);
    }

    public void start() {
    	ThreadCategory.setPrefix(ImporterService.NAME);
        m_status = Fiber.STARTING;
        ThreadCategory.getInstance().debug("SPRING: thread.classLoader="+Thread.currentThread().getContextClassLoader());

        BeanFactoryLocator bfl = DefaultLocatorFactory.getInstance();
        BeanFactoryReference bf = bfl.useBeanFactory("daoContext");
        ApplicationContext daoContext = (ApplicationContext) bf.getFactory();
        
        m_context = new ClassPathXmlApplicationContext(new String[] { "/org/opennms/netmgt/importer/importer-context.xml" }, daoContext);
        ThreadCategory.getInstance().debug("SPRING: context.classLoader="+m_context.getClassLoader());
        m_status = Fiber.RUNNING;
    }

    public void stop() {
    	ThreadCategory.setPrefix(ImporterService.NAME);
        m_status = Fiber.STOP_PENDING;
        m_context.close();
        
        
        m_status = Fiber.STOPPED;
    }

    public String status() {
    	ThreadCategory.setPrefix(ImporterService.NAME);
        return Fiber.STATUS_NAMES[m_status];
    }

	public int getStatus() {
		return m_status;
	}
	
	public String getStats() {
		return getImporterService().getStats();
	}

	private org.opennms.netmgt.importer.ImporterService getImporterService() {
		org.opennms.netmgt.importer.ImporterService importer = (org.opennms.netmgt.importer.ImporterService)m_context.getBean("modelImporter");
		return importer;
	}


}
