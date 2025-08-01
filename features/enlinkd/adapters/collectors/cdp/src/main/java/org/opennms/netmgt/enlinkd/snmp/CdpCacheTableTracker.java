/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.enlinkd.snmp;

import static org.opennms.core.utils.InetAddressUtils.getInetAddress;
import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;

import org.opennms.netmgt.enlinkd.model.CdpLink;
import org.opennms.netmgt.enlinkd.model.CdpLink.CiscoNetworkProtocolType;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TableTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CdpCacheTableTracker extends TableTracker {
	private static final Logger LOG = LoggerFactory.getLogger(CdpCacheTableTracker.class);

	public final static SnmpObjId CDP_CACHE_ADDRESS_TYPE_OID = SnmpObjId.get(".1.3.6.1.4.1.9.9.23.1.2.1.1.3");
	public final static SnmpObjId CDP_CACHE_ADDRESS_OID = SnmpObjId.get(".1.3.6.1.4.1.9.9.23.1.2.1.1.4");
	public final static SnmpObjId CDP_CACHE_VERSION_OID = SnmpObjId.get(".1.3.6.1.4.1.9.9.23.1.2.1.1.5");
	public final static SnmpObjId CDP_CACHE_DEVICE_ID_OID = SnmpObjId.get(".1.3.6.1.4.1.9.9.23.1.2.1.1.6");
	public final static SnmpObjId CDP_CACHE_DEVICE_PORT_OID = SnmpObjId.get(".1.3.6.1.4.1.9.9.23.1.2.1.1.7");
	public final static SnmpObjId CDP_CACHE_PLATFORM_OID = SnmpObjId.get(".1.3.6.1.4.1.9.9.23.1.2.1.1.8");

	public final static String CDP_CACHE_ADDRESS_TYPE = "cdpCacheAddressType" ;
	public final static String CDP_CACHE_ADDRESS = "cdpCacheAddress";
	public final static String CDP_CACHE_VERSION = "cdpCacheVersion";
	public final static String CDP_CACHE_DEVICE_ID = "cdpCacheDeviceId";
	public final static String CDP_CACHE_DEVICE_PORT = "cdpCacheDevicePort";
	public final static String CDP_CACHE_PLATFORM = "cdpCachePlatform";

	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the dot1dbasetable. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	public static final SnmpObjId[] cdpCache_elemList = new SnmpObjId[] {

		/*
		 * <P>An indication of the type of address contained in the
		 *  corresponding instance of cdpCacheAddress.</P>
		 */
			CDP_CACHE_ADDRESS_TYPE_OID,

		/*
		 * <P>The (first) network-layer address of the device's
		 *  SNMP-agent as reported in the Address TLV of the most recently
		 *  received CDP message. For example, if the corresponding
		 *  instance of cacheAddressType had the value 'ip(1)', then
		 *  this object would be an IP-address.</P>
		 */
			CDP_CACHE_ADDRESS_OID,

		/*
		 *     <P>The Version string as reported in the most recent CDP
         *   message.  The zero-length string indicates no Version
         *   field (TLV) was reported in the most recent CDP
         *   message.
		 */
			CDP_CACHE_VERSION_OID,
		/*
		 * <P>The Device-ID string as reported in the most recent CDP
		 *  message. The zero-length string indicates no Device-ID
		 *  field (TLV) was reported in the most recent CDP
		 *  message.</P>
		 */
			CDP_CACHE_DEVICE_ID_OID,

		/*
		 * <P>The Port-ID string as reported in the most recent CDP
		 *  message. This will typically be the value of the ifName
		 *  object (e.g., 'Ethernet0'). The zero-length string
		 *  indicates no Port-ID field (TLV) was reported in the
		 *  most recent CDP message.</P>
		 */
			CDP_CACHE_DEVICE_PORT_OID,
		
		/*
		 * The Device's Hardware Platform as reported in the most
         *   recent CDP message.  The zero-length string indicates
         *   that no Platform field (TLV) was reported in the most
         *   recent CDP message.
		 */
			CDP_CACHE_PLATFORM_OID
	};

    public static class CdpCacheRow extends SnmpRowResult {
		
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
		    return getValue(CDP_CACHE_ADDRESS_TYPE_OID).toInt();
		}
	
		/**
		 * <p>getCdpCacheAddress</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public byte[] getCdpCacheAddress() {
                    return getValue(CDP_CACHE_ADDRESS_OID).getBytes();
		}
		
		/**
		 * <p>getCdpCacheIpv4Address</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		private InetAddress getCdpCacheInetAddress() {
	            return getInetAddress(getCdpCacheAddress());	    
		}
		
		private String getDisplayableCdpCacheAddress() {
                    SnmpValue cdpCacheAddressValue = getValue(CDP_CACHE_ADDRESS_OID);
                    try {
                        if (cdpCacheAddressValue.isDisplayable())
                            return cdpCacheAddressValue.toDisplayString();
                    } catch (Exception e) {
                        return cdpCacheAddressValue.toHexString();
                    }
                    return "not able to diplay";
		}

		public String getCdpCacheAddressString() {
		    String cdpCacheAddressValueString;
			CiscoNetworkProtocolType type = CiscoNetworkProtocolType.get(getCdpCacheAddressType());
    		    switch (type) {
    		        case ip:
                        case ipv6:
                    try {
                        cdpCacheAddressValueString =str(getCdpCacheInetAddress());
                    } catch (Exception e) {
                        cdpCacheAddressValueString = getDisplayableCdpCacheAddress(); 
                    }
                            break;
                        default:
                            cdpCacheAddressValueString = getDisplayableCdpCacheAddress(); 
                            break;
                        }
		    return cdpCacheAddressValueString;
		}

		public String getCdpCacheVersion() {
			return getValue(CDP_CACHE_VERSION_OID).toDisplayString();
		}
		
		/**
		 * <p>getCdpCacheDeviceId</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String getCdpCacheDeviceId() {
		    if (getValue(CDP_CACHE_DEVICE_ID_OID).isDisplayable())
			return getValue(CDP_CACHE_DEVICE_ID_OID).toDisplayString();
		    return getValue(CDP_CACHE_DEVICE_ID_OID).toHexString();
		}
		
		/**
		 * <p>getCdpCacheDevicePort</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String getCdpCacheDevicePort() {
		    if (getValue(CDP_CACHE_DEVICE_PORT_OID).isDisplayable())
			return 	getValue(CDP_CACHE_DEVICE_PORT_OID).toDisplayString();
		    return getValue(CDP_CACHE_DEVICE_PORT_OID).toHexString();
		}
				
		public String getCdpCachePlatform() {
			return getValue(CDP_CACHE_PLATFORM_OID).toDisplayString();
		}

		public CdpLink getLink() {
            LOG.debug("processCdpCacheRow: row index: cdpCacheIfindex: {} cdpCacheDeviceIndex: {}",  
                     getCdpCacheIfIndex(), getCdpCacheDeviceIndex());
            CdpLink link = new CdpLink();
            
            link.setCdpCacheIfIndex(getCdpCacheIfIndex());
            link.setCdpCacheDeviceIndex(getCdpCacheDeviceIndex());
            link.setCdpCacheAddressType(CiscoNetworkProtocolType.get(getCdpCacheAddressType()));
            link.setCdpCacheAddress(getCdpCacheAddressString());
            link.setCdpCacheVersion(getCdpCacheVersion());
            link.setCdpCacheDeviceId(getCdpCacheDeviceId());
            link.setCdpCacheDevicePort(getCdpCacheDevicePort());
            link.setCdpCacheDevicePlatform(getCdpCachePlatform());
            return link;
	    }

    }

	/**
	 * <p>Constructor for CdpCacheTableEntry.</p>
	 */
	public CdpCacheTableTracker() {
		super(cdpCache_elemList);
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
		System.out.printf("\t\t%s (%s)= %s (%s)\n", CDP_CACHE_ADDRESS_TYPE_OID + "." + row.getInstance().toString(), CDP_CACHE_ADDRESS_TYPE, row.getCdpCacheAddressType(), CiscoNetworkProtocolType.get(row.getCdpCacheAddressType())  );
		System.out.printf("\t\t%s (%s)= %s \n", CDP_CACHE_ADDRESS_OID + "." + row.getInstance().toString(), CDP_CACHE_ADDRESS, row.getCdpCacheAddressString());
		System.out.printf("\t\t%s (%s)= %s \n", CDP_CACHE_VERSION_OID + "." + row.getInstance().toString(), CDP_CACHE_VERSION, row.getCdpCacheVersion());
		System.out.printf("\t\t%s (%s)= %s \n", CDP_CACHE_DEVICE_ID_OID + "." + row.getInstance().toString(), CDP_CACHE_DEVICE_ID, row.getCdpCacheDeviceId());
		System.out.printf("\t\t%s (%s)= %s \n", CDP_CACHE_DEVICE_PORT_OID + "." + row.getInstance().toString(), CDP_CACHE_DEVICE_PORT, row.getCdpCacheDevicePort());
		System.out.printf("\t\t%s (%s)= %s \n", CDP_CACHE_PLATFORM_OID + "." + row.getInstance().toString(), CDP_CACHE_PLATFORM, row.getCdpCachePlatform());
	}

}
