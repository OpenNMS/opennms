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

package org.opennms.netmgt.eventd.db;

import org.opennms.netmgt.model.events.Constants;
import org.opennms.netmgt.xml.event.Logmsg;

/**
 * This is an utility class used to format the event log message info - to be
 * inserted into the 'events' table
 *
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 */
public final class LogMessage {
    /**
     * Format the logmsg entry
     *
     * @param msg
     *            the logmsg
     * @return the formatted logmsg
     */
    public static String format(Logmsg msg) {
        String txt = Constants.escape(msg.getContent(), Constants.DB_ATTRIB_DELIM);
        String log = msg.getDest();

        String fmsg = txt + Constants.DB_ATTRIB_DELIM + log;
        if (fmsg.length() >= 256)
            fmsg = fmsg.substring(0, 252) + Constants.VALUE_TRUNCATE_INDICATOR;

        return fmsg;
    }
}
