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
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

import org.opennms.netmgt.xml.event.Snmp;

/**
 * This class is used to format the Snmp block into an appropiate string for
 * storage in the event data storage.
 *
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 */
public final class SnmpInfo {
    /**
     * <P>
     * Converts the SNMP iformation from the event into a string that can be
     * stored into the database. The information is formatted in by separating
     * the of the textual fields with a delimiter character (a comma ',').
     * </P>
     *
     * <P>
     * If the enterprise id text information is not present then the string will
     * have only two commas. An extra comma is not added to signify the missing
     * field.
     * </P>
     *
     * @see Constants#DB_ATTRIB_DELIM
     * @see Constants#escape
     * @see Constants#DB_ATTRIB_DELIM
     * @see Constants#escape
     * @return The smnpblock as a string
     * @param info a {@link org.opennms.netmgt.xml.event.Snmp} object.
     * @param maxlen a int.
     */
    public static String format(Snmp info, int maxlen) {
        if (info == null)
            return null;

        // id
        StringBuffer snmpStr = new StringBuffer(info.getId());

        // id text
        if (info.getIdtext() != null) {
            snmpStr.append(Constants.DB_ATTRIB_DELIM + Constants.escape(info.getIdtext(), Constants.DB_ATTRIB_DELIM));
        } else
            snmpStr.append(Constants.DB_ATTRIB_DELIM + "undefined");

        // version
        snmpStr.append(Constants.DB_ATTRIB_DELIM + info.getVersion());

        // specific
        if (info.hasSpecific())
            snmpStr.append(Constants.DB_ATTRIB_DELIM + Integer.toString(info.getSpecific()));
        else
            snmpStr.append(Constants.DB_ATTRIB_DELIM + "undefined");

        // generic
        if (info.hasGeneric())
            snmpStr.append(Constants.DB_ATTRIB_DELIM + Integer.toString(info.getGeneric()));
        else
            snmpStr.append(Constants.DB_ATTRIB_DELIM + "undefined");

        // community
        if (info.getCommunity() != null)
            snmpStr.append(Constants.DB_ATTRIB_DELIM + info.getCommunity());
        else
            snmpStr.append(Constants.DB_ATTRIB_DELIM + "undefined");

        return Constants.format(snmpStr.toString(), maxlen);
    }
}
