/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.tools.spectrum;

import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Mask;
import org.opennms.netmgt.xml.eventconf.Maskelement;

public class OidMapping {
    private String m_oid;
    private int m_eventVarNum;
    private int m_indexLength;
    
    private static final String s_oidExpr = "^\\.?([0-9]+\\.){3,}[0-9]+$";
    
    public OidMapping(String oid) {
        if (! oid.matches(s_oidExpr)) {
            throw new IllegalArgumentException("The OID must be of the form .1.3.6.1 or 1.3.6.1 and must be at least three octets in length");
        }
        m_oid = oid;
        m_eventVarNum = -1;
        m_indexLength = -1;
    }

    public OidMapping(String oid, int eventVarNum, int indexLength) {
        if (! oid.matches(s_oidExpr)) {
            throw new IllegalArgumentException("The OID must be of the form .1.3.6.1 or 1.3.6.1 and must be at least three octets in length");
        }
        m_oid = oid;
        m_eventVarNum = eventVarNum;
        m_indexLength = indexLength;
    }

    public String getOid() {
        return m_oid;
    }

    public void setOid(String oid) {
        if (oid == null) {
            throw new IllegalArgumentException("The OID must not be null");
        }
        if (! oid.matches(s_oidExpr)) {
            throw new IllegalArgumentException("The OID must be of the form .1.3.6.1 or 1.3.6.1 and must be at least three octets in length");
        }
        m_oid = oid;
    }

    public int getEventVarNum() {
        return m_eventVarNum;
    }

    public void setEventVarNum(int eventVarNum) {
        m_eventVarNum = eventVarNum;
    }

    public int getIndexLength() {
        return m_indexLength;
    }

    public void setIndexLength(int indexLength) {
        m_indexLength = indexLength;
    }

    public Event makeEvent() {
        Event evt = new Event();
        Mask mask = new Mask();

        // Trap-OID
        Maskelement me = new Maskelement();
        me.setMename("id");

        evt.setMask(mask);
        return evt;
    }
}