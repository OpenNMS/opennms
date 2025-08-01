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
package org.opennms.core.criteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class OrderBuilder {
	
	private static final Logger LOG = LoggerFactory.getLogger(OrderBuilder.class);
	
    private final Set<Order> m_orders = new LinkedHashSet<>();

    private String m_lastAttribute = null;

    /**
     * Append an order object to the end of the order list.
     * 
     * @param order
     *            the order object to append
     * @return whether it was added (true if added, false if already
     *         exists/ignored)
     */
    boolean append(final Order order) {
        if (m_orders.add(order)) {
            m_lastAttribute = order.getAttribute();
            return true;
        } else {
            m_lastAttribute = null;
            return false;
        }
    }

    public void clear() {
        m_orders.clear();
    }

    public Collection<Order> getOrderCollection() {
        // make a copy so the internal one can't be modified outside of the
        // builder
        return new ArrayList<Order>(m_orders);
    }

    public void asc() {
        synchronized (m_orders) {
            if (m_orders.isEmpty()) {
            	LOG.debug("asc() called, but no orderBy has been specified.");
            } else if (m_lastAttribute == null) {
            	LOG.debug("asc() called on an attribute that can't be changed.");
            } else {
                for (final Order o : m_orders) {
                    if (o.getAttribute().equals(m_lastAttribute)) {
                        m_orders.remove(o);
                        m_orders.add(Order.asc(m_lastAttribute));
                        break;
                    }
                }
            }
        }
    }

    public void desc() {
        synchronized (m_orders) {
            if (m_orders.isEmpty()) {
            	LOG.debug("desc() called, but no orderBy has been specified.");
            } else if (m_lastAttribute == null) {
            	LOG.debug("desc() called on an attribute that can't be changed.");
            } else {
                for (final Order o : m_orders) {
                    if (o.getAttribute().equals(m_lastAttribute)) {
                        m_orders.remove(o);
                        m_orders.add(Order.desc(m_lastAttribute));
                        break;
                    }
                }
            }
        }
    }

}