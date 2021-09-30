/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp;

public class InstanceListTracker extends AggregateTracker {
    
    public InstanceListTracker(SnmpObjId base, String instances) {
        this(base, SnmpInstId.convertToSnmpInstIds(instances), null);
    }
    
    public InstanceListTracker(SnmpObjId base, String instances, CollectionTracker parent) {
        this(base, SnmpInstId.convertToSnmpInstIds(instances), parent);
    }
    
    public InstanceListTracker(SnmpObjId base, SnmpInstId[] instances) {
        this(base, instances, null);
    }
    
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
