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

import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.EventReceipt;

/**
 * This interface provides the contract that implementor must implement in order
 * to receive events from adaptors.
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http;//www.opennms.org">OpenNMS </a>
 * @version $Id: $
 */
public interface EventHandler {
    /**
     * <p>processEvent</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a boolean.
     */
    public boolean processEvent(Event event);

    /**
     * <p>receiptSent</p>
     *
     * @param receipt a {@link org.opennms.netmgt.xml.event.EventReceipt} object.
     */
    public void receiptSent(EventReceipt receipt);
}
