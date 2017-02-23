/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.events.snmp;

import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;

/**
 * <p>SyntaxToEvent class.</p>
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
    private SyntaxToEvent(int typeId, String type) {
        m_typeId = typeId;
        m_type = type;
    }

    /**
     * <p>getTypeId</p>
     *
     * @return a int.
     */
    private int getTypeId() {
        return m_typeId;
    }

    /**
     * <p>getType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    private String getType() {
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
    public static Parm processSyntax(final String name, final SnmpValue value) {
    	final Value val = new Value();

        boolean found = false;
        for (int i = 0; i < m_syntaxToEvents.length; i++) {
            if (m_syntaxToEvents[i].getTypeId() == -1 || m_syntaxToEvents[i].getTypeId() == value.getType()) {
                val.setType(m_syntaxToEvents[i].getType());
                String encoding = null;
                boolean displayable = false;
                try {
                    displayable = value.isDisplayable();
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                    // Eat it
                    // This should not be necessary when NMS-7547 is fixed
                }
                if (displayable) {
                    if (name.matches(".*[Mm][Aa][Cc].*")) {
                        encoding = EventConstants.XML_ENCODING_MAC_ADDRESS;
                    } else {
                        encoding = EventConstants.XML_ENCODING_TEXT;
                    }
                } else {
                    if (value.getBytes().length == 6) {
                        encoding = EventConstants.XML_ENCODING_MAC_ADDRESS;
                    } else {
                        encoding = EventConstants.XML_ENCODING_BASE64;
                    }
                }
                val.setEncoding(encoding);
                val.setContent(EventConstants.toString(encoding, value));
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IllegalStateException("Internal error: fell through the " + "bottom of the loop.  The syntax-to-events array might not have a " + "catch-all for Object");
        }

        final Parm parm = new Parm();
        parm.setParmName(name);
        parm.setValue(val);

        return parm;
    }
}
