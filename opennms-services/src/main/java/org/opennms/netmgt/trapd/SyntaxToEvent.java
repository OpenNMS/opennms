//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.trapd;

import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;

/**
 * <p>SyntaxToEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SyntaxToEvent {
    int m_typeId;

    String m_type;

    /** Constant <code>m_syntaxToEvents</code> */
    public static SyntaxToEvent[] m_syntaxToEvents;

    static {
        setupSyntax();
    }

    /**
     * <p>Constructor for SyntaxToEvent.</p>
     *
     * @param typeId a int.
     * @param type a {@link java.lang.String} object.
     */
    public SyntaxToEvent(int typeId, String type) {
        m_typeId = typeId;
        m_type = type;
    }

    /**
     * <p>getTypeId</p>
     *
     * @return a int.
     */
    public int getTypeId() {
        return m_typeId;
    }

    /**
     * <p>getType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getType() {
        return m_type;
    }

    /**
     * <p>setupSyntax</p>
     */
    public static void setupSyntax() {
        m_syntaxToEvents = new SyntaxToEvent[] { 
                new SyntaxToEvent(SnmpValue.SNMP_INT32,             EventConstants.TYPE_SNMP_INT32),
                new SyntaxToEvent(SnmpValue.SNMP_NULL,              EventConstants.TYPE_SNMP_NULL), 
                new SyntaxToEvent(SnmpValue.SNMP_OBJECT_IDENTIFIER, EventConstants.TYPE_SNMP_OBJECT_IDENTIFIER), 
                new SyntaxToEvent(SnmpValue.SNMP_IPADDRESS,         EventConstants.TYPE_SNMP_IPADDRESS), 
                new SyntaxToEvent(SnmpValue.SNMP_TIMETICKS,         EventConstants.TYPE_SNMP_TIMETICKS), 
                new SyntaxToEvent(SnmpValue.SNMP_COUNTER32,         EventConstants.TYPE_SNMP_COUNTER32),
                new SyntaxToEvent(SnmpValue.SNMP_GAUGE32,           EventConstants.TYPE_SNMP_GAUGE32), 
                new SyntaxToEvent(SnmpValue.SNMP_OCTET_STRING,      EventConstants.TYPE_SNMP_OCTET_STRING), 
                new SyntaxToEvent(SnmpValue.SNMP_OPAQUE,            EventConstants.TYPE_SNMP_OPAQUE), 
                new SyntaxToEvent(SnmpValue.SNMP_COUNTER64,         EventConstants.TYPE_SNMP_COUNTER64),
                new SyntaxToEvent(-1,                               EventConstants.TYPE_STRING) 
        };
    }

    /**
     * <p>processSyntax</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param value a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     * @return a {@link org.opennms.netmgt.xml.event.Parm} object.
     */
    public static Parm processSyntax(String name, SnmpValue value) {
        Value val = new Value();

        boolean found = false;
        for (int i = 0; i < m_syntaxToEvents.length; i++) {
            if (m_syntaxToEvents[i].getTypeId() == -1 || m_syntaxToEvents[i].getTypeId()== value.getType()) {
                val.setType(m_syntaxToEvents[i].getType());
                String encoding = value.isDisplayable() ? EventConstants.XML_ENCODING_TEXT : EventConstants.XML_ENCODING_BASE64;
                val.setEncoding(encoding);
                val.setContent(EventConstants.toString(encoding, value));
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IllegalStateException("Internal error: fell through the " + "bottom of the loop.  The syntax-to-events array might not have a " + "catch-all for Object");
        }

        Parm parm = new Parm();
        parm.setParmName(name);
        parm.setValue(val);

        return parm;
    }
}
