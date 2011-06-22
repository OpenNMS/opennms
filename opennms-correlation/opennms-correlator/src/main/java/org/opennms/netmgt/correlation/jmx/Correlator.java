/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 2 of the License,
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

package org.opennms.netmgt.correlation.jmx;

import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.access.DefaultLocatorFactory;


/**
 * <p>Correlator class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Correlator implements CorrelatorMBean {
    
    org.opennms.netmgt.correlation.Correlator m_correlator;

	/**
	 * <p>init</p>
	 */
	public void init() {
		final BeanFactoryLocator bfl = DefaultLocatorFactory.getInstance();
        final BeanFactoryReference bf = bfl.useBeanFactory("correlatorContext");
        m_correlator = (org.opennms.netmgt.correlation.Correlator) bf.getFactory().getBean("correlator");
    }
    
    private org.opennms.netmgt.correlation.Correlator getBean() {
		return m_correlator;
	}

    /**
     * <p>start</p>
     */
    public void start() {
        getBean().start();
    }

    /**
     * <p>stop</p>
     */
    public void stop() {
        getBean().stop();
    }

    /**
     * <p>getStatus</p>
     *
     * @return a int.
     */
    public int getStatus() {
        return getBean().getStatus();
    }

    /**
     * <p>getStatusText</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStatusText() {
        return org.opennms.core.fiber.Fiber.STATUS_NAMES[getStatus()];
    }

    /**
     * <p>status</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String status() {
        return org.opennms.core.fiber.Fiber.STATUS_NAMES[getStatus()];
    }
}
