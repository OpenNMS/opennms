/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.linkd;

import org.opennms.netmgt.xml.event.Event;

/**
 * Provides a collection of utility methods used by Linkd Processor
 * for dealing with Events
 *
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 * @version $Id: $
 */
public final class EventUtils {
    
    /**
     * Ensures the given event has an interface or ifIndex
     *
     * @param e
     *            the event
     * @throws org.opennms.netmgt.linkd.InsufficientInformationException
     *             if  neither an interface nor an ifIndex is available
     */
    static public void checkInterfaceOrIfIndex(Event e) throws InsufficientInformationException {
        if (e == null)
            throw new NullPointerException("e is null");

        if (e.getInterface() == null) {
            if (!e.hasIfIndex()) {
                throw new InsufficientInformationException("Both ipaddr and ifIndex for event are unavailable");
            }
        }
    }

    /**
     * Ensures that the given Event has a node id
     *
     * @param e
     *            the event
     * @throws org.opennms.netmgt.linkd.InsufficientInformationException
     *             if a node id is not available
     */
    static public void checkNodeId(Event e) throws InsufficientInformationException {
        if (e == null) {
            throw new NullPointerException("e is null");
        }

        if (!e.hasNodeid()) {
            throw new InsufficientInformationException("nodeid for event is unavailable");
        }
    }
}
