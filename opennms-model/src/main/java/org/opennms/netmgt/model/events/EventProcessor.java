/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.events;

import java.sql.SQLException;

import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Header;


/**
 * Event processor interface.  Classes that want to modify or react to
 * events within eventd implement this interface and are dependency
 * injected into the eventProcessors List in EventHandler.
 *
 * @author ranger
 * @version $Id: $
 */
public interface EventProcessor {
    /**
     * <p>process</p>
     *
     * @param eventHeader a {@link org.opennms.netmgt.xml.event.Header} object.
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @throws java.sql.SQLException if any.
     */
    void process(Header eventHeader, Event event) throws SQLException;
}
