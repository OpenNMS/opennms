//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.eventd.db;

import org.opennms.netmgt.model.events.Constants;

/**
 * This is an utility class used to format the event forward info - to be
 * inserted into the 'events' table
 * 
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class Forward {
    /**
     * Format each forward entry
     * 
     * @param fwd
     *            the entry
     * 
     * @return the formatted string
     */
    public static String format(org.opennms.netmgt.xml.event.Forward fwd) {
        String text = fwd.getContent();
        String state = fwd.getState();

        String how = fwd.getMechanism();

        return Constants.escape(text, Constants.DB_ATTRIB_DELIM) + Constants.DB_ATTRIB_DELIM + state + Constants.DB_ATTRIB_DELIM + how;

    }

    /**
     * Format the array of forward entries of the event
     * 
     * @param forwards
     *            the list
     * @param sz
     *            the size to which the formatted string is to be limited
     *            to(usually the size of the column in the database)
     * 
     * @return the formatted string
     */
    public static String format(org.opennms.netmgt.xml.event.Forward[] forwards, int sz) {
        StringBuffer buf = new StringBuffer();
        boolean first = true;

        for (int index = 0; index < forwards.length; index++) {
            if (!first)
                buf.append(Constants.MULTIPLE_VAL_DELIM);
            else
                first = false;

            buf.append(Constants.escape(format(forwards[index]), Constants.MULTIPLE_VAL_DELIM));
        }

        if (buf.length() >= sz) {
            buf.setLength(sz - 4);
            buf.append(Constants.VALUE_TRUNCATE_INDICATOR);
        }

        return buf.toString();
    }
}
