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

import static org.opennms.core.utils.InetAddressUtils.getInetAddress;
import static org.opennms.core.utils.InetAddressUtils.normalize;
import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.snmp.RowCallback;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TableTracker;

/**
 * PhysInterfaceTableTracker
 *
 * @author brozow
 * @version $Id: $
 */
public class IPAddressTableTracker extends TableTracker {
    
	public static final SnmpObjId IP_ADDRESS_PREFIX_TABLE_ENTRY = SnmpObjId.get(".1.3.6.1.2.1.4.32.1");
    public static final SnmpObjId IP_ADDRESS_TABLE_ENTRY = SnmpObjId.get(".1.3.6.1.2.1.4.34.1");
    
    public static final SnmpObjId IP_ADDRESS_IF_INDEX = SnmpObjId.get(IP_ADDRESS_TABLE_ENTRY, "3");
    public static final SnmpObjId IP_ADDRESS_TYPE_INDEX = SnmpObjId.get(IP_ADDRESS_TABLE_ENTRY, "4");
    public static final SnmpObjId IP_ADDRESS_PREFIX_INDEX = SnmpObjId.get(IP_ADDRESS_TABLE_ENTRY, "5");
    public static final SnmpObjId IP_ADDRESS_PREFIX_ORIGIN_INDEX = SnmpObjId.get(IP_ADDRESS_PREFIX_TABLE_ENTRY, "5");
    public static final SnmpObjId IP_ADDRESS_ORIGIN_INDEX = SnmpObjId.get(IP_ADDRESS_TABLE_ENTRY, "6");
    public static final SnmpObjId IP_ADDRESS_STATUS_INDEX = SnmpObjId.get(IP_ADDRESS_TABLE_ENTRY, "7");
    public static final SnmpObjId IP_ADDRESS_CREATED_INDEX = SnmpObjId.get(IP_ADDRESS_TABLE_ENTRY, "8");
    public static final SnmpObjId IP_ADDRESS_LAST_CHANGED_INDEX = SnmpObjId.get(IP_ADDRESS_TABLE_ENTRY, "9");
    public static final SnmpObjId IP_ADDRESS_ROW_STATUS_INDEX = SnmpObjId.get(IP_ADDRESS_TABLE_ENTRY, "10");
    public static final SnmpObjId IP_ADDRESS_STORAGE_TYPE_INDEX = SnmpObjId.get(IP_ADDRESS_TABLE_ENTRY, "11");
	public static final int TYPE_IPV4 = 1;
	public static final int TYPE_IPV6 = 2;

    private static SnmpObjId[] s_tableColumns = new SnmpObjId[] {
        IP_ADDRESS_IF_INDEX,
        IP_ADDRESS_PREFIX_INDEX
    };
    
    class IPAddressRow extends SnmpRowResult {

        public IPAddressRow(final int columnCount, final SnmpInstId instance) {
            super(columnCount, instance);
            LogUtils.debugf(this, "column count = %d, instance = %s", columnCount, instance);
        }
        
        public Integer getIfIndex() {
        	final SnmpValue value = getValue(IP_ADDRESS_IF_INDEX);
        	return value.toInt();
        }
        
        public String getIpAddress() {
        	final SnmpResult result = getResult(IP_ADDRESS_IF_INDEX);
        	final int[] instanceIds = result.getInstance().getIds();
        	
        	final int addressType = instanceIds[0];
        	final int addressLength = instanceIds[1];
			if (addressType == TYPE_IPV4 || addressType == TYPE_IPV6) {
				final InetAddress address = getInetAddress(instanceIds, 2, addressLength);
				return str(address);
			}
            return null;
        }

        private String getNetMask() {
        	final SnmpValue value = getValue(IP_ADDRESS_PREFIX_INDEX);
        	
        	final SnmpObjId netmaskRef = value.toSnmpObjId().getInstance(IP_ADDRESS_PREFIX_ORIGIN_INDEX);
        	
        	final int[] rawIds = netmaskRef.getIds();
        	final int addressType = rawIds[1];
        	final int addressLength = rawIds[2];
        	final InetAddress address = getInetAddress(rawIds, 3, addressLength);
        	final int mask = rawIds[rawIds.length - 1];

			if (addressType == TYPE_IPV4 || addressType == TYPE_IPV6) {
				return str(address) + "/" + mask;
        	} else {
        		LogUtils.warnf(this, "unknown address type, expected 1 (IPv4) or 2 (IPv6), but got %d", addressType);
        		return null;
        	}
        }

		public OnmsIpInterface createInterfaceFromRow() {
            
        	final Integer ifIndex = getIfIndex();
        	final String ipAddr = getIpAddress();
        	final String netMask = getNetMask();
            
        	LogUtils.debugf(this, "createInterfaceFromRow: ifIndex = %s, ipAddress = %s, netmask = %s", ifIndex, ipAddr, netMask);

        	final OnmsSnmpInterface snmpIface = new OnmsSnmpInterface(null, ifIndex);
            snmpIface.setNetMask(netMask);
            snmpIface.setCollectionEnabled(true);
            
            final OnmsIpInterface iface = new OnmsIpInterface(ipAddr, null);
            iface.setSnmpInterface(snmpIface);
            
            iface.setIfIndex(ifIndex);
        	final String hostName = normalize(ipAddr);
        	LogUtils.debugf(this, "setIpHostName: %s", hostName);
        	iface.setIpHostName(hostName == null? ipAddr : hostName);
            
            return iface;
        }

        private SnmpResult getResult(final SnmpObjId base) {
            for(final SnmpResult result : getResults()) {
                if (base.equals(result.getBase())) {
                    return result;
                }
            }
            
            return null;
        }

    }
    
    /**
     * <p>Constructor for IPInterfaceTableTracker.</p>
     */
    public IPAddressTableTracker() {
        super(s_tableColumns);
    }

    /**
     * <p>Constructor for IPInterfaceTableTracker.</p>
     *
     * @param rowProcessor a {@link org.opennms.netmgt.snmp.RowCallback} object.
     */
    public IPAddressTableTracker(final RowCallback rowProcessor) {
        super(rowProcessor, s_tableColumns);
    }
    
    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new IPAddressRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processIPAddressRow((IPAddressRow)row);
    }

    /**
     * <p>processIPInterfaceRow</p>
     *
     * @param row a {@link org.opennms.netmgt.provision.service.IPAddressTableTracker.IPAddressRow} object.
     */
    public void processIPAddressRow(final IPAddressRow row) {
        
    }

}
