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



public class SnmpColumn {

    private InstanceTracker m_tracker;
    private Object m_result = null;

    /*
     * TODO track the process of retrieving a column.
     * TODO MibObject used to define the column
     * TODO hasNext returns true until the last value or one passed the last is set
     * TODO getNext returns the next var to set
     * TODO addResult set the last retrieved value
     * TODO addResult must be able to take results out of order ?
     * 
     * 
     */
    
    public SnmpColumn(String baseOid, String instances) {
        m_tracker = new SpecificInstanceTracker(baseOid, instances);
    }

    public boolean hasOidForNext() {
        return m_tracker.hasOidForNext();
    }

    public String getOidForNext() {
        return m_tracker.getOidForNext();
    }

    public void addResult(String oid, Object val) {
        if (m_tracker.receivedOid(oid) != null)
            m_result = val;
        
    }

    public Object getResultForInstance(String string) {
       return m_result;
    }

}
