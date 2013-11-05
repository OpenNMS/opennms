/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.openoss.opennms.spring.qosd.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.DefaultLocatorFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * This JMX bean loads the QoSD daemon as a spring bean using the
 * qosd-context.xml file.
 *
 * @author ranger
 * @version $Id: $
 */
public class QoSD extends AbstractServiceDaemon implements QoSDMBean {
    private static final Logger LOG = LoggerFactory.getLogger(QoSD.class);

	/**
	 * <p>Constructor for QoSD.</p>
	 */
	public QoSD() {
		super(NAME);
	}

	private static final String NAME = org.openoss.opennms.spring.qosd.QoSDimpl2.NAME;

	private ClassPathXmlApplicationContext m_context;

	// used only for testing
	ApplicationContext getContext() {
		return m_context;
	}

	/**
	 * <p>onInit</p>
	 */
        @Override
	protected void onInit() {}


	/**
	 * <p>onStart</p>
	 */
        @Override
	protected void onStart() {


//TODO REMOVE EXAMPLE IMPORTER CODE
//		ThreadCategory.setPrefix(ImporterService.NAME);
//		m_status = Fiber.STARTING;
//		ThreadCategory.getInstance().debug("SPRING: thread.classLoader="+Thread.currentThread().getContextClassLoader());

//		BeanFactoryLocator bfl = DefaultLocatorFactory.getInstance();
//		BeanFactoryReference bf = bfl.useBeanFactory("daoContext");
//		ApplicationContext daoContext = (ApplicationContext) bf.getFactory();

//		m_context = new ClassPathXmlApplicationContext(new String[] { "/org/opennms/netmgt/importer/importer-context.xml" }, daoContext);
//		ThreadCategory.getInstance().debug("SPRING: context.classLoader="+m_context.getClassLoader());
//		m_status = Fiber.RUNNING;

		LOG.debug("SPRING: thread.classLoader={}", Thread.currentThread().getContextClassLoader());

		// finds the already instantiated OpenNMS daoContext
		BeanFactoryLocator bfl = DefaultLocatorFactory.getInstance();
		BeanFactoryReference bf = bfl.useBeanFactory("daoContext");
		ApplicationContext daoContext = (ApplicationContext) bf.getFactory();


		// this chooses if we expect AlarmMonitor to run in seperate j2ee container ( Jboss ) or in local
		// OpenNMS spring container
		String qosdj2ee=System.getProperty("qosd.usej2ee");
		LOG.info("QoSD System Property qosd.usej2ee={}", qosdj2ee);
		if ("true".equals(qosdj2ee)){
			LOG.debug("QoSD using /org/openoss/opennms/spring/qosd/qosd-j2ee-context.xml");
			m_context = new ClassPathXmlApplicationContext(new String[] { "/org/openoss/opennms/spring/qosd/qosd-j2ee-context.xml" },daoContext);
		}
		else {
			LOG.debug("QoSD using /org/openoss/opennms/spring/qosd/qosd-spring-context.xml");
			m_context = new ClassPathXmlApplicationContext(new String[] { "/org/openoss/opennms/spring/qosd/qosd-spring-context.xml" },daoContext);
		}

		LOG.debug("SPRING: context.classLoader={}", m_context.getClassLoader());

		getQoSD().init();
		getQoSD().start();
	}


	/**
	 * <p>onStop</p>
	 */
        @Override
	protected void onStop() {
		getQoSD().stop();

		m_context.close();
	}

	/**
	 * <p>getStats</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getStats() {
		return getQoSD().getStats();
	}

	/**
	 * Returns the qosd singleton
	 * @return qosd
	 */
	private org.openoss.opennms.spring.qosd.QoSD getQoSD() {
		org.openoss.opennms.spring.qosd.QoSD qosd = (org.openoss.opennms.spring.qosd.QoSD)m_context.getBean("QoSD");
		qosd.setApplicationContext(m_context); // pass in local spring application context
		return qosd;
	}


}
