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


public abstract class InstanceTracker  {
    
    /**
     * Returns the base oid for the tracker used for computing instances
     * @return the base oid 
     */
    public abstract SnmpObjId getBaseOid();

    /**
     * Returns true if the tracker still needs to receive more results to determine 
     * if it has gotten all the data.
     * @return true if a get next is needed, false otherwise
     */
    public abstract boolean hasOidForNext();

    /**
     * Returns an oid to request in a getNext or a getBulk to get additional data for
     * this tracker
     * @return the oid to request
     * @throws IllegalStateException if no oid is needed
     */
    public abstract SnmpObjId getOidForNext();

    /**
     * Notifies the tracker of the receipt of an oid.  
     * @param lastOid the oid received
     * @return null if the oid does not belong to the set of instances tracked by this 
     * tracker, the instance if it does belong
     */
    public abstract SnmpInstId receivedOid(SnmpObjId lastOid);
    
    /**
     * 
     */
    public static InstanceTracker get(SnmpObjId base, String instances) {
        if ("ifIndex".equals(instances)) {
            return new ColumnInstanceTracker(base);
        } else {
            return new SpecificInstanceTracker(base, instances);
        }
    }
    
}
