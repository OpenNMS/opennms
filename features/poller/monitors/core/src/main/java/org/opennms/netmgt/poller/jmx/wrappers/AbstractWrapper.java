/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
