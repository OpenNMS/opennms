/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.collectd;

import org.opennms.netmgt.snmp.SingleInstanceTracker;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;

public class ObjIdMonitor extends SingleInstanceTracker {
    SnmpValue value;

    public ObjIdMonitor(SnmpObjId base, SnmpInstId inst) {
        super(base, inst);
        value = null;
    }
    
    SnmpValue getValue() {
        return value;
    }
    
    int getIntValue() {
        return (value == null ? -1 : value.toInt());
    }
    
    long getLongValue() {
        return (value == null ? -1L : value.toLong());
    }

    @Override
    protected void storeResult(SnmpResult res) {
        value = res.getValue();
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append(getClass().getName());
        buffer.append("@");
        buffer.append(Integer.toHexString(hashCode()));
        buffer.append(": value: " + getValue());
        
        return buffer.toString();
    }

}
