//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.netmgt.collectd.jmx;

import org.opennms.netmgt.collectd.CollectorConfigDaoImpl;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.access.DefaultLocatorFactory;
import org.springframework.transaction.support.TransactionTemplate;

public class Collectd implements CollectdMBean {
	org.opennms.netmgt.collectd.Collectd m_bean = null;

	private org.opennms.netmgt.collectd.Collectd getBean() {
		if (m_bean == null)
			m_bean = new org.opennms.netmgt.collectd.Collectd();
		
		return m_bean;
	}


	public void init() {
        BeanFactoryLocator bfl = DefaultLocatorFactory.getInstance();
        BeanFactoryReference bf = bfl.useBeanFactory("daoContext");
		MonitoredServiceDao monitoredServiceDao = (MonitoredServiceDao) bf.getFactory().getBean("monitoredServiceDao");
		IpInterfaceDao ipInterfaceDao = (IpInterfaceDao) bf.getFactory().getBean("ipInterfaceDao");
		NodeDao nodeDao = (NodeDao) bf.getFactory().getBean("nodeDao");
		TransactionTemplate transTemplate = (TransactionTemplate) bf.getFactory().getBean("transactionTemplate");

		getBean().setCollectorConfigDao(new CollectorConfigDaoImpl());
		getBean().setMonitoredServiceDao(monitoredServiceDao);
		getBean().setIpInterfaceDao(ipInterfaceDao);
		getBean().setNodeDao(nodeDao);
		getBean().setEventIpcManager(EventIpcManagerFactory.getIpcManager());
		getBean().setTransactionTemplate(transTemplate);

        getBean().init();
    }

    public void start() {
        getBean().start();
    }

    public void stop() {
        getBean().stop();
    }

    public int getStatus() {
        return getBean().getStatus();
    }

    public String getStatusText() {
        return org.opennms.core.fiber.Fiber.STATUS_NAMES[getStatus()];
    }

    public String status() {
        return org.opennms.core.fiber.Fiber.STATUS_NAMES[getStatus()];
    }
}
