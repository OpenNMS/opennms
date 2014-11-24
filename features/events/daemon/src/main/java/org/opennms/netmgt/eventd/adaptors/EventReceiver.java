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

package org.opennms.netmgt.eventd.adaptors;

import org.opennms.core.fiber.InitializableFiber;

/**
 * This interface defines the contract that every type of adaptor event receiver
 * must fulfill. Every receiver is a fiber and will run independently of other
 * receivers in the system. When an event is received by an instance of this
 * interface it will pass the new event to the <code>EventHandler.event()</code>
 * method.
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http;//www.opennms.org">OpenNMS </a>
 */
public interface EventReceiver extends InitializableFiber {
    /**
     * Adds a new event handler to receiver. When new events are received the
     * decoded event is passed to the handler.
     *
     * @param handler
     *            A reference to an event handler
     */
    void addEventHandler(EventHandler handler);

    /**
     * Removes an event handler from the list of handler called when an event is
     * received. The handler is removed based upon the method
     * <code>equals()</code> inherieted from the <code>Object</code> class.
     *
     * @param handler
     *            A reference to the event handler.
     */
    void removeEventHandler(EventHandler handler);
}
