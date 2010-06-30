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
package org.opennms.netmgt.snmp;


/**
 * <p>InstanceListTracker class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class InstanceListTracker extends AggregateTracker {
    
    /**
     * <p>Constructor for InstanceListTracker.</p>
     *
     * @param base a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @param instances a {@link java.lang.String} object.
     */
    public InstanceListTracker(SnmpObjId base, String instances) {
        this(base, SnmpInstId.convertToSnmpInstIds(instances), null);
    }
    
    /**
     * <p>Constructor for InstanceListTracker.</p>
     *
     * @param base a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @param instances a {@link java.lang.String} object.
     * @param parent a {@link org.opennms.netmgt.snmp.CollectionTracker} object.
     */
    public InstanceListTracker(SnmpObjId base, String instances, CollectionTracker parent) {
        this(base, SnmpInstId.convertToSnmpInstIds(instances), parent);
    }
    
    /**
     * <p>Constructor for InstanceListTracker.</p>
     *
     * @param base a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @param instances an array of {@link org.opennms.netmgt.snmp.SnmpInstId} objects.
     */
    public InstanceListTracker(SnmpObjId base, SnmpInstId[] instances) {
        this(base, instances, null);
    }
    
    /**
     * <p>Constructor for InstanceListTracker.</p>
     *
     * @param base a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @param instances an array of {@link org.opennms.netmgt.snmp.SnmpInstId} objects.
     * @param parent a {@link org.opennms.netmgt.snmp.CollectionTracker} object.
     */
    public InstanceListTracker(SnmpObjId base, SnmpInstId[] instances, CollectionTracker parent) {
        super(getSingleInstanceTrackers(base, instances), parent);
    }
    
    private static SingleInstanceTracker[] getSingleInstanceTrackers(SnmpObjId base, SnmpInstId[] instances) {
        SingleInstanceTracker[] trackers = new SingleInstanceTracker[instances.length];
        for (int i = 0; i < instances.length; i++) {
            trackers[i] = new SingleInstanceTracker(base, instances[i]);
        }
        return trackers;
        
    }

}
