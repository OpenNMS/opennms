/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
                if (value.isDisplayable()) {
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
