/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.snmp;

import java.net.InetAddress;

import static org.opennms.core.utils.InetAddressUtils.getIpAddressByHexString;
import static org.opennms.core.utils.InetAddressUtils.str;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.CdpLink.CiscoNetworkProtocolType;
import org.opennms.netmgt.snmp.RowCallback;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.TableTracker;

public class CdpCacheTableTracker extends TableTracker {
	private static final Logger LOG = LoggerFactory.getLogger(CdpCacheTableTracker.class);
	
	public static final SnmpObjId CDP_CACHE_TABLE_ENTRY = SnmpObjId.get(".1.3.6.1.4.1.9.9.23.1.2.1.1"); // start of table (GETNEXT)

	public final static SnmpObjId CDP_CACHE_ADDRESS_TYPE      = SnmpObjId.get(".1.3.6.1.4.1.9.9.23.1.2.1.1.3");
	public final static SnmpObjId CDP_CACHE_ADDRESS           = SnmpObjId.get(".1.3.6.1.4.1.9.9.23.1.2.1.1.4");
	public final static SnmpObjId CDP_CACHE_VERSION           = SnmpObjId.get(".1.3.6.1.4.1.9.9.23.1.2.1.1.5");
	public final static SnmpObjId CDP_CACHE_DEVICE_ID         = SnmpObjId.get(".1.3.6.1.4.1.9.9.23.1.2.1.1.6");
	public final static SnmpObjId CDP_CACHE_DEVICE_PORT       = SnmpObjId.get(".1.3.6.1.4.1.9.9.23.1.2.1.1.7");
	public final static SnmpObjId CDP_CACHE_PLATFORM          = SnmpObjId.get(".1.3.6.1.4.1.9.9.23.1.2.1.1.8");
                                                           
	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the dot1dbasetable. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	public static final SnmpObjId[] cdpCache_elemList = new SnmpObjId[] {

		/**
		 * <P>An indication of the type of address contained in the
		 *  corresponding instance of cdpCacheAddress.</P>
		 */
		CDP_CACHE_ADDRESS_TYPE,

		/**
		 * <P>The (first) network-layer address of the device's
		 *  SNMP-agent as reported in the Address TLV of the most recently
		 *  received CDP message. For example, if the corresponding
		 *  instance of cacheAddressType had the value 'ip(1)', then
		 *  this object would be an IP-address.</P>
		 */
		CDP_CACHE_ADDRESS, 

		/**
		 *     <P>The Version string as reported in the most recent CDP
         *   message.  The zero-length string indicates no Version
         *   field (TLV) was reported in the most recent CDP
         *   message.
		 */
		CDP_CACHE_VERSION,
		/**
		 * <P>The Device-ID string as reported in the most recent CDP
		 *  message. The zero-length string indicates no Device-ID
		 *  field (TLV) was reported in the most recent CDP
		 *  message.</P>
		 */
		CDP_CACHE_DEVICE_ID,

		/**
		 * <P>The Port-ID string as reported in the most recent CDP
		 *  message. This will typically be the value of the ifName
		 *  object (e.g., 'Ethernet0'). The zero-length string
		 *  indicates no Port-ID field (TLV) was reported in the
		 *  most recent CDP message.</P>
		 */
		CDP_CACHE_DEVICE_PORT,
		
		/**
		 * The Device's Hardware Platform as reported in the most
         *   recent CDP message.  The zero-length string indicates
         *   that no Platform field (TLV) was reported in the most
         *   recent CDP message.
		 */
		CDP_CACHE_PLATFORM
	};

    public class CdpCacheRow extends SnmpRowResult {
		
    	public CdpCacheRow(int columnCount, SnmpInstId instance) {
			super(columnCount, instance);
            LOG.debug("column count = {}, instance = {}", columnCount, instance);
		}

    	/**
    	 *  "Normally, the ifIndex value of the local interface.
         *   For 802.3 Repeaters for which the repeater ports do not
         *   have ifIndex values assigned, this value is a unique
         *   value for the port, and greater than any ifIndex value
         *   supported by the repeater; the specific port number in
         *   this case, is given by the corresponding value of
         *   cdpInterfacePort."
    	 * 
    	 */
    	
    	public Integer getCdpCacheIfIndex() {
    	    return getInstance().getSubIdAt(getInstance().length()-2);
    	}

        public Integer getCdpCacheDeviceIndex() {
            return getInstance().getLastSubId();
        }

		/**
		 * <p>getCdpCacheAddressType</p>
		 *
		 * @return a int.
		 */
		public Integer getCdpCacheAddressType() {
		    return getValue(CDP_CACHE_ADDRESS_TYPE).toInt();
		}
	
		/**
		 * <p>getCdpCacheAddress</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String getCdpCacheAddress() {
		    return getValue(CDP_CACHE_ADDRESS).toHexString();
		}
	
		/**
		 * <p>getCdpCacheIpv4Address</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public InetAddress getCdpCacheIpv4Address() {
	            return getIpAddressByHexString(getCdpCacheAddress());	    
		}
				

		public String getCdpCacheVersion() {
			return getValue(CDP_CACHE_VERSION).toDisplayString();
		}
		
		/**
		 * <p>getCdpCacheDeviceId</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String getCdpCacheDeviceId() {
		    if (getValue(CDP_CACHE_DEVICE_ID).isDisplayable())
			return getValue(CDP_CACHE_DEVICE_ID).toDisplayString();
		    return getValue(CDP_CACHE_DEVICE_ID).toHexString();
		}
		
		/**
		 * <p>getCdpCacheDevicePort</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String getCdpCacheDevicePort() {
		    if (getValue(CDP_CACHE_DEVICE_PORT).isDisplayable())
			return 	getValue(CDP_CACHE_DEVICE_PORT).toDisplayString();
		    return getValue(CDP_CACHE_DEVICE_PORT).toHexString();
		}
				
		public String getCdpCachePlatform() {
			return getValue(CDP_CACHE_PLATFORM).toDisplayString();
		}

		public CdpLink getLink(CdpInterfacePortNameGetter cdpInterfacePortNameGetter) {
            LOG.info("processCdpCacheRow: row index: cdpCacheIfindex: {} cdpCacheDeviceIndex: {}",  
                     getCdpCacheIfIndex(), getCdpCacheDeviceIndex());
            CdpLink link = new CdpLink();
            
            link.setCdpCacheIfIndex(getCdpCacheIfIndex());
            link.setCdpCacheDeviceIndex(getCdpCacheDeviceIndex());
            link.setCdpCacheAddressType(CiscoNetworkProtocolType.get(getCdpCacheAddressType()));
            if (CiscoNetworkProtocolType.ip == link.getCdpCacheAddressType())
            	link.setCdpCacheAddress(str(getCdpCacheIpv4Address()));
            else 
            	link.setCdpCacheAddress(getCdpCacheAddress());
            link.setCdpCacheVersion(getCdpCacheVersion());
            link.setCdpCacheDeviceId(getCdpCacheDeviceId());
            link.setCdpCacheDevicePort(getCdpCacheDevicePort());
            link.setCdpCacheDevicePlatform(getCdpCachePlatform());
            return cdpInterfacePortNameGetter.get(link);
	    }

    }

	/**
	 * <p>Constructor for CdpCacheTableEntry.</p>
	 */
	public CdpCacheTableTracker() {
		super(cdpCache_elemList);
	}

	/**
	 * <p>Constructor for CdpCacheTableEntry.</p>
	 */
	public CdpCacheTableTracker(RowCallback rowProcessor) {
		super(rowProcessor,cdpCache_elemList);
	}

    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new CdpCacheRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processCdpCacheRow((CdpCacheRow)row);
    }

    /**
     * <p>processcdpCacheRow</p>
     *
     * @param row a {@link org.opennms.netmgt.enlinkd.snmp.CdpCacheTableTracker.CdpCacheRow} object.
     */
    public void processCdpCacheRow(final CdpCacheRow row) {
    }

}
