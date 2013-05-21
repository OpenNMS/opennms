/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.correlation.jmx;

import org.opennms.core.fiber.Fiber;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.access.DefaultLocatorFactory;

public class Correlator implements CorrelatorMBean {

    org.opennms.netmgt.correlation.Correlator m_correlator;

    /**
     * Initialization.
     * 
     * Retrieves the Spring context for the correlator.
     */
    @Override
    public void init() {
        final BeanFactoryLocator bfl = DefaultLocatorFactory.getInstance();
        final BeanFactoryReference bf = bfl.useBeanFactory("correlatorContext");
        m_correlator = (org.opennms.netmgt.correlation.Correlator) bf.getFactory().getBean("correlator");
    }

    private org.opennms.netmgt.correlation.Correlator getBean() {
        return m_correlator;
    }

    /**
     * Start the correlator daemon.
     */
    @Override
    public void start() {
        if (getBean() != null) getBean().start();
    }

    /**
     * Stop the correlator daemon.
     */
    @Override
    public void stop() {
        if (getBean() != null) getBean().stop();
    }

    /**
     * Get the current status of the correlator daemon.
     * 
     * @return The integer constant from {@link Fiber} that represents the daemon's status.
     */
    @Override
    public int getStatus() {
        return getBean() == null? Fiber.STOPPED : getBean().getStatus();
    }

    /**
     * Get the current status of the correlator.
     * 
     * @return The status, as text.
     */
    @Override
    public String getStatusText() {
        return Fiber.STATUS_NAMES[getStatus()];
    }

    /**
     * Get the current status of the correlator.
     * 
     * @return The status, as text.
     */
    @Override
    public String status() {
        return Fiber.STATUS_NAMES[getStatus()];
    }
}
