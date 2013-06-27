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

package org.opennms.netmgt.xmlrpcd.jmx;

import org.apache.xmlrpc.XmlRpc;
import org.opennms.core.fiber.Fiber;
import org.opennms.core.utils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <p>Provisioner class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Provisioner implements ProvisionerMBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(Provisioner.class);

    private ClassPathXmlApplicationContext m_context;
    int m_status = Fiber.START_PENDING;
    
    // used only for testing
    ApplicationContext getContext() {
        return m_context;
    }

    /**
     * <p>init</p>
     */
    @Override
    public void init() {
        XmlRpc.debug = "true".equalsIgnoreCase(System.getProperty("xmlrpc.debug", "false"));
    }

    /**
     * <p>start</p>
     */
    @Override
    public void start() {
        m_status = Fiber.STARTING;
        LOG.debug("SPRING: thread.classLoader=", Thread.currentThread().getContextClassLoader());
        m_context = BeanUtils.getFactory("provisionerContext", ClassPathXmlApplicationContext.class);
        LOG.debug("SPRING: context.classLoader=", m_context.getClassLoader());
        m_status = Fiber.RUNNING;
    }

    /**
     * <p>stop</p>
     */
    @Override
    public void stop() {
        m_status = Fiber.STOP_PENDING;
        m_context.close();
        
        
        m_status = Fiber.STOPPED;
    }

    /**
     * Returns the status of this Fiber.
     *
     * @return a int.
     */
    @Override
    public int getStatus() {
        return m_status;
    }

    /**
     * <p>getStatusText</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getStatusText() {
        return Fiber.STATUS_NAMES[m_status];
    }

    /**
     * <p>status</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String status() {
        return getStatusText();
    }
}
