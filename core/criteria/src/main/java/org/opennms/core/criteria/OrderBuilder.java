/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.core.criteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.opennms.core.utils.LogUtils;

public final class OrderBuilder {
    private final LinkedHashSet<Order> m_orders = new LinkedHashSet<Order>();

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
                LogUtils.debugf(this, "asc() called, but no orderBy has been specified.");
            } else if (m_lastAttribute == null) {
                LogUtils.debugf(this, "asc() called on an attribute that can't be changed.");
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
                LogUtils.debugf(this, "desc() called, but no orderBy has been specified.");
            } else if (m_lastAttribute == null) {
                LogUtils.debugf(this, "desc() called on an attribute that can't be changed.");
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