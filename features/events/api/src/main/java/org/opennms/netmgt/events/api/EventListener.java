/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.events.api;

import org.opennms.netmgt.xml.event.Event;

/**
 * The interface to be implemented by all services that wish to receive events
 * from Eventd.
 *
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 */
public interface EventListener {
    /**
     * Return the id of the listener
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName();

    /**
     * Process a sent event.
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public void onEvent(Event e);
}
