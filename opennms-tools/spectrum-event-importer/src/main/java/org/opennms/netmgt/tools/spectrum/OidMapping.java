/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 9, 2010
 *
 * Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
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