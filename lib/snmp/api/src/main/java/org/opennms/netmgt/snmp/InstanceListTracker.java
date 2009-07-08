/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2005-2006, 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
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
