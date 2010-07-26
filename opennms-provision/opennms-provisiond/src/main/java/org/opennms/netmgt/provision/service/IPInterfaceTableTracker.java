/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.service;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.snmp.RowCallback;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TableTracker;

/**
 * PhysInterfaceTableTracker
 *
 * @author brozow
 * @version $Id: $
 */
public class IPInterfaceTableTracker extends TableTracker {
    
    /** Constant <code>IP_ADDR_TABLE_ENTRY</code> */
    public static final SnmpObjId IP_ADDR_TABLE_ENTRY = SnmpObjId.get(".1.3.6.1.2.1.4.20.1");
    
    /** Constant <code>IP_ADDR_ENT_ADDR</code> */
    public static final SnmpObjId IP_ADDR_ENT_ADDR = SnmpObjId.get(IP_ADDR_TABLE_ENTRY, "1");
    /** Constant <code>IP_ADDR_IF_INDEX</code> */
    public static final SnmpObjId IP_ADDR_IF_INDEX = SnmpObjId.get(IP_ADDR_TABLE_ENTRY, "2");
    /** Constant <code>IP_ADDR_ENT_NETMASK</code> */
    public static final SnmpObjId IP_ADDR_ENT_NETMASK = SnmpObjId.get(IP_ADDR_TABLE_ENTRY, "3");
    /** Constant <code>IP_ADDR_ENT_BCASTADDR</code> */
    public static final SnmpObjId IP_ADDR_ENT_BCASTADDR = SnmpObjId.get(IP_ADDR_TABLE_ENTRY, "4");
    
    
    private static SnmpObjId[] s_tableColumns = new SnmpObjId[] {
        IP_ADDR_ENT_ADDR,
        IP_ADDR_IF_INDEX,
        IP_ADDR_ENT_NETMASK,
        IP_ADDR_ENT_BCASTADDR
    };
    
    class IPInterfaceRow extends SnmpRowResult {

        public IPInterfaceRow(int columnCount, SnmpInstId instance) {
            super(columnCount, instance);
        }
        
        public Integer getIfIndex() {
            SnmpValue value = getValue(IP_ADDR_IF_INDEX);
            return value == null ? null : value.toInt();
        }
        
        public String getIpAddress() {
            SnmpValue value = getValue(IP_ADDR_ENT_ADDR);
            if (value != null) {
                return value.toInetAddress().getHostAddress();
            } else {
                // instance for ipAddr Table it ipAddr
                SnmpInstId inst = getInstance();
                if (inst != null) {
                    try {
                        String ipAddr = inst.toString();
                        return InetAddress.getByName(ipAddr).getHostAddress();
                    } catch (UnknownHostException e) {
                        throw new IllegalArgumentException("cannot convert "+inst+" to an InetAddress"); 
                    }
                }
            }
            return null;
        }

        private String getNetMask() {
            SnmpValue value = getValue(IP_ADDR_ENT_NETMASK);
            return value == null ? null : value.toInetAddress().getHostAddress();
        }

        public OnmsIpInterface createInterfaceFromRow() {
            
            String ipAddr = getIpAddress();
            String netMask = getNetMask();
            Integer ifIndex = getIfIndex();
            
            OnmsSnmpInterface snmpIface = new OnmsSnmpInterface(ipAddr, ifIndex, null);
            snmpIface.setNetMask(netMask);
            snmpIface.setCollectionEnabled(true);
            
            OnmsIpInterface iface = new OnmsIpInterface(ipAddr, null);
            iface.setSnmpInterface(snmpIface);
            
            iface.setIfIndex(ifIndex);
            try {
                iface.setIpHostName(InetAddress.getByName(ipAddr).getHostName());
            } catch (UnknownHostException e) {
                iface.setIpHostName(ipAddr);
            }
            
            return iface;
        }

    }
    
    /**
     * <p>Constructor for IPInterfaceTableTracker.</p>
     */
    public IPInterfaceTableTracker() {
        super(s_tableColumns);
    }

    /**
     * <p>Constructor for IPInterfaceTableTracker.</p>
     *
     * @param rowProcessor a {@link org.opennms.netmgt.snmp.RowCallback} object.
     */
    public IPInterfaceTableTracker(RowCallback rowProcessor) {
        super(rowProcessor, s_tableColumns);
    }
    
    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(int columnCount, SnmpInstId instance) {
        return new IPInterfaceRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(SnmpRowResult row) {
        processIPInterfaceRow((IPInterfaceRow)row);
    }

    /**
     * <p>processIPInterfaceRow</p>
     *
     * @param row a {@link org.opennms.netmgt.provision.service.IPInterfaceTableTracker.IPInterfaceRow} object.
     */
    public void processIPInterfaceRow(IPInterfaceRow row) {
        
    }

}
