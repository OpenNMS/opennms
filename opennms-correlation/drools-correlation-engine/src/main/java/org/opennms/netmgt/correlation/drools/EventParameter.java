/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.correlation.drools;

import java.io.Serializable;

import org.opennms.netmgt.xml.event.Event;

/**
 * <p>EventParameter class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class EventParameter implements Serializable {
    private static final long serialVersionUID = -1084189255958969146L;

    private String m_name;
    private Object m_value;
    private Event m_event;
    
    /**
     * <p>getEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event getEvent() {
        return m_event;
    }
    /**
     * <p>setEvent</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public void setEvent(final Event event) {
        m_event = event;
    }
    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }
    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(final String name) {
        m_name = name;
    }
    /**
     * <p>getValue</p>
     *
     * @return a {@link java.lang.Object} object.
     */
    public Object getValue() {
        return m_value;
    }
    /**
     * <p>setValue</p>
     *
     * @param value a {@link java.lang.Object} object.
     */
    public void setValue(final Object value) {
        m_value = value;
    }
}
