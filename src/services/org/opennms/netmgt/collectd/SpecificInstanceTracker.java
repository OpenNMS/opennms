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
package org.opennms.netmgt.collectd;

import java.util.StringTokenizer;

import org.opennms.protocols.snmp.SnmpObjectId;

public class SpecificInstanceTracker implements InstanceTracker {
    
    private int[] m_instances;
    private int m_current = 0;
    private String m_base;

    public SpecificInstanceTracker(String base, String instance) {
        m_base = base;
        StringTokenizer tokenizer = new StringTokenizer(instance, ",");
        m_instances = new int[tokenizer.countTokens()];
        int index = 0;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            int inst = Integer.parseInt(token);
            m_instances[index] = inst;
            index++;
        }
    }

    public boolean hasOidForNext() {
        return m_current < m_instances.length;
    }

    public String getOidForNext() {
        return nextExpectedInstance() == 0 ? m_base : m_base+"."+(nextExpectedInstance()-1);
    }

    private int nextExpectedInstance() {
        return m_instances[m_current];
    }

    public String receivedOid(String lastOid) {
        int expectedInstance = nextExpectedInstance();
        SnmpObjectId expectedOid = new SnmpObjectId(m_base+"."+expectedInstance);
        SnmpObjectId receivedOid = new SnmpObjectId(lastOid);
        m_current++;
        return expectedOid.equals(receivedOid) ? Integer.toString(expectedInstance) : null;
    }

    public String getBaseOid() {
        return m_base;
    }

}
