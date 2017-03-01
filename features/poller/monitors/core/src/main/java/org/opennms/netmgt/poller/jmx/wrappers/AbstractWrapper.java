/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.netmgt.poller.jmx.wrappers;

import javax.management.MBeanServerConnection;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularDataSupport;

/**
 * Base class for all wrappers used to evaluate MBean attributes in JEXL expressions.
 */
public abstract class AbstractWrapper {
    /**
     * The connection to the MBean server.
     */
    protected final MBeanServerConnection connection;

    public AbstractWrapper(final MBeanServerConnection connection) {
        this.connection = connection;
    }

    /**
     * Get the value of the requested attribute.
     *
     * Implementors should wrap the result using the {@link #wrap(Object)} method.
     *
     * @param name the name of the requested attribute
     * @return the value
     */
    public abstract Object get(final String name);

    /**
     * Helper to wrap an arbitary result in a wrapper if required.
     *
     * @param result the result to wrap
     * @return a wrapper or the result itself
     */
    protected Object wrap(final Object result) {
        if (result instanceof TabularDataSupport) {
            return new TabularDataWrapper(this.connection, (TabularDataSupport) result);
        }

        if (result instanceof CompositeDataSupport) {
            return new CompositeDataWrapper(this.connection, (CompositeDataSupport) result);
        }

        return result;
    }
}
