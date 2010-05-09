// This file is part of the OpenNMS(R) QoSD OSS/J interface.
//
// Copyright (C) 2006-2007 Craig Gallen, 
//                         University of Southampton,
//                         School of Electronics and Computer Science
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// See: http://www.fsf.org/copyleft/lesser.html
//

package org.openoss.opennms.spring.qosdrx.jmx;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.DefaultLocatorFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * This JMX bean loads the QoSDrx daemon as a spring bean using the 
 * qosdrx-context.xml file.
 */
public class QoSDrx extends AbstractServiceDaemon implements QoSDrxMBean {

	public QoSDrx() {
		super(NAME);
	}

	private static final String NAME = org.openoss.opennms.spring.qosdrx.QoSDrx.NAME;

	private ClassPathXmlApplicationContext m_context;

	// used only for testing
	ApplicationContext getContext() {
		return m_context;
	}

	protected void onInit() {}


	protected void onStart() {
//		TODO REMOVE EXAMPLE IMPORTER CODE
//		ThreadCategory.setPrefix(ImporterService.NAME);
//		m_status = Fiber.STARTING;
//		ThreadCategory.getInstance().debug("SPRING: thread.classLoader="+Thread.currentThread().getContextClassLoader());

//		BeanFactoryLocator bfl = DefaultLocatorFactory.getInstance();
//		BeanFactoryReference bf = bfl.useBeanFactory("daoContext");
//		ApplicationContext daoContext = (ApplicationContext) bf.getFactory();

//		m_context = new ClassPathXmlApplicationContext(new String[] { "/org/opennms/netmgt/importer/importer-context.xml" }, daoContext);
//		ThreadCategory.getInstance().debug("SPRING: context.classLoader="+m_context.getClassLoader());
//		m_status = Fiber.RUNNING;
		
		
		
		ThreadCategory.getInstance().debug("SPRING: thread.classLoader="+Thread.currentThread().getContextClassLoader());

		// finds the already instantiated OpenNMS daoContext
		BeanFactoryLocator bfl = DefaultLocatorFactory.getInstance();
		BeanFactoryReference bf = bfl.useBeanFactory("daoContext");
		ApplicationContext daoContext = (ApplicationContext) bf.getFactory();
	
		ThreadCategory.getInstance().debug("QoSDrx using /org/openoss/opennms/spring/qosdrx/qosdrx-spring-context.xml");
		m_context = new ClassPathXmlApplicationContext(new String[] { "/org/openoss/opennms/spring/qosdrx/qosdrx-spring-context.xml" }, daoContext);
		ThreadCategory.getInstance().debug("SPRING: context.classLoader="+m_context.getClassLoader());

		getQoSDrx().init();
		getQoSDrx().start();

//TODO remove old code		
//		ThreadCategory.setPrefix(QoSDrx.NAME);
//		m_status = Fiber.STARTING;
//		ThreadCategory.getInstance().debug("SPRING: thread.classLoader="+Thread.currentThread().getContextClassLoader());
//
//		ThreadCategory.getInstance().debug("QoSDrx using /org/openoss/opennms/spring/qosdrx/qosdrx-spring-context.xml");
//		m_context = new ClassPathXmlApplicationContext(new String[] { "/org/openoss/opennms/spring/qosdrx/qosdrx-spring-context.xml" });
//		ThreadCategory.getInstance().debug("SPRING: context.classLoader="+m_context.getClassLoader());
//
//		getQoSDrx().init();
//		getQoSDrx().start();
//
//
//		m_status = Fiber.RUNNING;
	}


	protected void onStop() {
		getQoSDrx().stop();

		m_context.close();
	}
	
	
	/**
	 * Method to return statistics from the running receivers to MX4J
	 * @return string representation of the statistics for the running receivers
	 */
	public String getRuntimeStatistics(){
		return getQoSDrx().getRuntimeStatistics();
	}
	
	

	public String getStats() {
		return getQoSDrx().getStats();
	}

	/**
	 * Returns the qosdrx singleton
	 * @return qosdrx
	 */
	private org.openoss.opennms.spring.qosdrx.QoSDrx getQoSDrx() {
		org.openoss.opennms.spring.qosdrx.QoSDrx qosdrx = (org.openoss.opennms.spring.qosdrx.QoSDrx)m_context.getBean("QoSDrx");
		qosdrx.setApplicationContext(m_context);
		return qosdrx;
	}

}
