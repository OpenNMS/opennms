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

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;
import org.opennms.protocols.snmp.SnmpCounter32;
import org.opennms.protocols.snmp.SnmpCounter64;
import org.opennms.protocols.snmp.SnmpGauge32;
import org.opennms.protocols.snmp.SnmpIPAddress;
import org.opennms.protocols.snmp.SnmpInt32;
import org.opennms.protocols.snmp.SnmpNull;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpOpaque;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpTimeTicks;

public class SyntaxToEvent {
    Class m_classMatch;

    String m_type;

    String m_encoding;

    public static SyntaxToEvent[] m_syntaxToEvents;

    static {
        setupSyntax();
    }

    public SyntaxToEvent(Class classMatch, String type, String encoding) {
        m_classMatch = classMatch;
        m_type = type;
        m_encoding = encoding;
    }

    public Class getClassMatch() {
        return m_classMatch;
    }

    public String getType() {
        return m_type;
    }

    public String getEncoding() {
        return m_encoding;
    }

    public static void setupSyntax() {
        m_syntaxToEvents = new SyntaxToEvent[] { 
                new SyntaxToEvent(SnmpInt32.class, EventConstants.TYPE_SNMP_INT32, EventConstants.XML_ENCODING_TEXT),
                new SyntaxToEvent(SnmpNull.class, EventConstants.TYPE_SNMP_NULL, EventConstants.XML_ENCODING_TEXT), 
                new SyntaxToEvent(SnmpObjectId.class, EventConstants.TYPE_SNMP_OBJECT_IDENTIFIER, EventConstants.XML_ENCODING_TEXT), 
                new SyntaxToEvent(SnmpIPAddress.class, EventConstants.TYPE_SNMP_IPADDRESS, EventConstants.XML_ENCODING_TEXT), 
                new SyntaxToEvent(SnmpTimeTicks.class, EventConstants.TYPE_SNMP_TIMETICKS, EventConstants.XML_ENCODING_TEXT), 
                new SyntaxToEvent(SnmpCounter32.class, EventConstants.TYPE_SNMP_COUNTER32, EventConstants.XML_ENCODING_TEXT),
                new SyntaxToEvent(SnmpGauge32.class, EventConstants.TYPE_SNMP_GAUGE32, EventConstants.XML_ENCODING_TEXT), 
                new SyntaxToEvent(SnmpOpaque.class, EventConstants.TYPE_SNMP_OPAQUE, EventConstants.XML_ENCODING_BASE64), 
                new SyntaxToEvent(SnmpCounter64.class, EventConstants.TYPE_SNMP_COUNTER64, EventConstants.XML_ENCODING_TEXT),
                new SyntaxToEvent(Object.class, EventConstants.TYPE_STRING, EventConstants.XML_ENCODING_TEXT) 
        };
    }

    public static Parm processSyntax(String name, SnmpSyntax obj) {
        Category log = ThreadCategory.getInstance(SyntaxToEvent.class);
        Value val = new Value();

        if (obj instanceof SnmpOctetString) {
            //
            // check for non-printable characters. If they
            // exist then print the string out as hexidecimal
            //
            boolean asHex = false;
            byte[] data = ((SnmpOctetString) obj).getString();
            for (int x = 0; x < data.length; x++) {
                byte b = data[x];
                if ((b < 32 && b != 9 && b != 10 && b != 13 && b != 0) || b == 127) {
                    asHex = true;
                    break;
                }
            }

            data = null;

            String encoding = asHex ? EventConstants.XML_ENCODING_BASE64 : EventConstants.XML_ENCODING_TEXT;

            val.setType(EventConstants.TYPE_SNMP_OCTET_STRING);
            val.setEncoding(encoding);
            val.setContent(EventConstants.toString(encoding, obj));

            // DEBUG
            if (!asHex && log.isDebugEnabled()) {
                log.debug("snmpReceivedTrap: string varbind: " + (((SnmpOctetString) obj).toString()));
            }
        } else {
            boolean found = false;
            for (int i = 0; i < m_syntaxToEvents.length; i++) {
                if (m_syntaxToEvents[i].getClassMatch() == null || m_syntaxToEvents[i].m_classMatch.isInstance(obj)) {
                    val.setType(m_syntaxToEvents[i].getType());
                    val.setEncoding(m_syntaxToEvents[i].getEncoding());
                    val.setContent(EventConstants.toString(m_syntaxToEvents[i].getType(), obj));
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalStateException("Internal error: fell through the " + "bottom of the loop.  The syntax-to-events array might not have a " + "catch-all for Object");
            }
        }

        Parm parm = new Parm();
        parm.setParmName(name);
        parm.setValue(val);

        return parm;
    }
}