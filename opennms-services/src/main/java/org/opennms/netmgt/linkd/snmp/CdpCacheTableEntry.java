/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.linkd.snmp;

import java.net.InetAddress;

import org.opennms.core.utils.InetAddressUtils;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpUtils;

/**
 *<P>The CdpCacheTableEntry class is designed to hold all the MIB-II
 * information for one entry in the
 * .iso.org.dod.internet.private.enterprises.cisco.ciscoMgmt.
 * ciscoCdpMIB.ciscoCdpMIBObjects.cdpCache.cdpCacheTable.cdpCacheEntry </P>
 * <P>This object is used by the CdpCacheTable  to hold information
 * single entries in the table. See the CdpCacheTable documentation
 * form more information.</P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio</A>
 * @see CdpCacheTable
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 * @version $Id: $
 */
public final class CdpCacheTableEntry extends SnmpStore {

	// Lookup strings for specific table entries
	//
	/** Constant <code>CDP_IFINDEX="cdpCacheIfIndex"</code> */
	public final static String CDP_IFINDEX = "cdpCacheIfIndex";
	/** Constant <code>CDP_DEVICEINDEX="cdpCacheDeviceIndex"</code> */
	public final static String CDP_DEVICEINDEX = "cdpCacheDeviceIndex";
	/** Constant <code>CDP_ADDRESS_TYPE="cdpCacheAddressType"</code> */
	public final static String CDP_ADDRESS_TYPE = "cdpCacheAddressType";
	/** Constant <code>CDP_ADDRESS="cdpCacheAddress"</code> */
	public final static String CDP_ADDRESS = "cdpCacheAddress";
	/** Constant <code>CDP_VERSION="cdpCacheVersion"</code> */
	public final static String CDP_VERSION = "cdpCacheVersion";
	/** Constant <code>CDP_DEVICEID="cdpCacheDeviceId"</code> */
	public final static String CDP_DEVICEID = "cdpCacheDeviceId";
	/** Constant <code>CDP_DEVICEPORT="cdpCacheDevicePort"</code> */
	public final static String CDP_DEVICEPORT = "cdpCacheDevicePort";
	/** Constant <code>CDP_PLATFORM="cdpPlatform"</code> */
	public final static String CDP_PLATFORM = "cdpPlatform";
	/** Constant <code>CDP_CAPS="cdpCacheCapabilities"</code> */
	public final static String CDP_CAPS = "cdpCacheCapabilities";
	/** Constant <code>CDP_VTP_MGMTDOMAIN="cdpCacheVtpMgmtDomain"</code> */
	public final static String CDP_VTP_MGMTDOMAIN = "cdpCacheVtpMgmtDomain";
	/** Constant <code>CDP_NATIVEVLAN="cdpCacheNatveVlan"</code> */
	public final static String CDP_NATIVEVLAN = "cdpCacheNatveVlan";
	/** Constant <code>CDP_DUPLEX="cdpCacheDuplex"</code> */
	public final static String CDP_DUPLEX = "cdpCacheDuplex";
	/** Constant <code>CDP_APPLIANCEID="cdpCacheApplianceID"</code> */
	public final static String CDP_APPLIANCEID = "cdpCacheApplianceID";
	/** Constant <code>CDP_VLANID="cdpCacheVlanID"</code> */
	public final static String CDP_VLANID = "cdpCacheVlanID";
	/** Constant <code>CDP_POWERCONS="cdpCachePowerConsumption"</code> */
	public final static String CDP_POWERCONS = "cdpCachePowerConsumption";
	/** Constant <code>CDP_MTU="cdpCacheMTU"</code> */
	public final static String CDP_MTU = "cdpCacheMTU";
	/** Constant <code>CDP_SYSNAME="cdpCacheSysName"</code> */
	public final static String CDP_SYSNAME = "cdpCacheSysName";
	/** Constant <code>CDP_SYSOBJID="cdpCacheSysObjectID"</code> */
	public final static String CDP_SYSOBJID = "cdpCacheSysObjectID";
	/** Constant <code>CDP_PRIMARYMGMTADDR_TYPE="cdpCachePrimaryMgmtAddressType"</code> */
	public final static String CDP_PRIMARYMGMTADDR_TYPE = "cdpCachePrimaryMgmtAddressType";
	/** Constant <code>CDP_PRIMARYMGMTADDR="cdpCachePrimaryMgmtAddress"</code> */
	public final static String CDP_PRIMARYMGMTADDR = "cdpCachePrimaryMgmtAddress";
	/** Constant <code>CDP_SECONDARYMGMTADDR_TYPE="cdpCacheSecondaryMgmtAddressType"</code> */
	public final static String CDP_SECONDARYMGMTADDR_TYPE = "cdpCacheSecondaryMgmtAddressType";
	/** Constant <code>CDP_SECONDARYMGMTADDR="cdpCacheSecondaryMgmtAddress"</code> */
	public final static String CDP_SECONDARYMGMTADDR = "cdpCacheSecondaryMgmtAddress";
	/** Constant <code>CDP_PHYSLOC="cdpCachePhysLocation"</code> */
	public final static String CDP_PHYSLOC = "cdpCachePhysLocation";
	/** Constant <code>CDP_LASTCHANGE="cdpCacheLastChange"</code> */
	public final static String CDP_LASTCHANGE = "cdpCacheLastChange";

	private boolean hasIfIndex = false;

	private final static String CDP_IFINDEX_OID = ".1.3.6.1.4.1.9.9.23.1.2.1.1.1";
        private final static String CDP_DEVICEINDEX_OID = ".1.3.6.1.4.1.9.9.23.1.2.1.1.2";
                                                           
	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the dot1dbasetable. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	public static final NamedSnmpVar[] cdpCache_elemList = new NamedSnmpVar[] {
		/**
		 * <P>Normally, the ifIndex value of the local interface.
		 * For 802.3 Repeaters for which the repeater ports do not
		 * have ifIndex values assigned, this value is a unique
		 * value for the port, and greater than any ifIndex value
		 * supported by the repeater; the specific port number in
		 * this case, is given by the corresponding value of
		 * cdpInterfacePort.</P>
		 */
		new NamedSnmpVar(NamedSnmpVar.SNMPINT32, CDP_IFINDEX, CDP_IFINDEX_OID, 1),

		/**
		 * <P>A unique value for each device from which CDP messages
		 * are being received.</P>
		 */
		new NamedSnmpVar(NamedSnmpVar.SNMPINT32, CDP_DEVICEINDEX, CDP_DEVICEINDEX_OID, 2),

		/**
		 * <P>An indication of the type of address contained in the
		 *  corresponding instance of cdpCacheAddress.</P>
		 */
		new NamedSnmpVar(NamedSnmpVar.SNMPINT32, CDP_ADDRESS_TYPE, ".1.3.6.1.4.1.9.9.23.1.2.1.1.3", 3),

		/**
		 * <P>The (first) network-layer address of the device's
		 *  SNMP-agent as reported in the Address TLV of the most recently
		 *  received CDP message. For example, if the corresponding
		 *  instance of cacheAddressType had the value 'ip(1)', then
		 *  this object would be an IP-address.</P>
		 */
		new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, CDP_ADDRESS, ".1.3.6.1.4.1.9.9.23.1.2.1.1.4", 4),

		/**
		 * <P>The Version string as reported in the most recent CDP
		 *  message. The zero-length string indicates no Version
		 *  field (TLV) was reported in the most recent CDP
		 *  message.</P>
		 */
		new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, CDP_VERSION, ".1.3.6.1.4.1.9.9.23.1.2.1.1.5", 5),

		/**
		 * <P>The Device-ID string as reported in the most recent CDP
		 *  message. The zero-length string indicates no Device-ID
		 *  field (TLV) was reported in the most recent CDP
		 *  message.</P>
		 */
		new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, CDP_DEVICEID, ".1.3.6.1.4.1.9.9.23.1.2.1.1.6", 6),

		/**
		 * <P>The Port-ID string as reported in the most recent CDP
		 *  message. This will typically be the value of the ifName
		 *  object (e.g., 'Ethernet0'). The zero-length string
		 *  indicates no Port-ID field (TLV) was reported in the
		 *  most recent CDP message.</P>
		 */
		new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, CDP_DEVICEPORT, ".1.3.6.1.4.1.9.9.23.1.2.1.1.7", 6)

		/**
		 * <P>The Device's Hardware Platform as reported in the most
		 *  recent CDP message. The zero-length string indicates
		 *  that no Platform field (TLV) was reported in the most
		 *  recent CDP message.</P>
		 */
		// new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, CDP_PLATFORM, ".1.3.6.1.4.1.9.9.23.1.2.1.1.8", 8),

		/**
		 * <P>The Device's Functional Capabilities as reported in the
		 *  most recent CDP message. For latest set of specific
		 *  values, see the latest version of the CDP specification.
		 *  The zero-length string indicates no Capabilities field
		 *  (TLV) was reported in the most recent CDP message.</P>
		 */
		// new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, CDP_CAPS, ".1.3.6.1.4.1.9.9.23.1.2.1.1.9", 9),

		/**
		 * <P>The VTP Management Domain for the remote device's interface,
		 *  as reported in the most recently received CDP message.
		 *  This object is not instantiated if no VTP Management Domain field
		 *  (TLV) was reported in the most recently received CDP message.</P>
		 */
		// new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, CDP_VTP_MGMTDOMAIN, ".1.3.6.1.4.1.9.9.23.1.2.1.1.10", 10),

		/**
		 * <P>The remote device's interface's native VLAN, as reported in the
		 *  most recent CDP message. The value 0 indicates
		 *  no native VLAN field (TLV) was reported in the most
		 *  recent CDP message.</P>
		 */
		// new NamedSnmpVar(NamedSnmpVar.SNMPINT32, CDP_NATIVEVLAN, ".1.3.6.1.4.1.9.9.23.1.2.1.1.11", 11),

		/**
		 * <P>The remote device's interface's duplex mode, as reported in the 
		 * most recent CDP message. The value unknown(1) indicates
		 * no duplex mode field (TLV) was reported in the most
		 *  recent CDP message.</P>
		 * <P>
		 * unknown    (1)
		 * halfduplex (2)
		 * fullduplex (3) </P> 
		 */
		// new NamedSnmpVar(NamedSnmpVar.SNMPINT32, CDP_DUPLEX, ".1.3.6.1.4.1.9.9.23.1.2.1.1.12", 12),

		/**
		 * <P>The remote device's Appliance ID, as reported in the
		 *  most recent CDP message. This object is not instantiated if
		 *  no Appliance VLAN-ID field (TLV) was reported in the most
		 *  recently received CDP message.</P>
		 *  
		 */
		// new NamedSnmpVar(NamedSnmpVar.SNMPGAUGE32, CDP_APPLIANCEID, ".1.3.6.1.4.1.9.9.23.1.2.1.1.13", 13),

		/**
		 * <P>The remote device's VoIP VLAN ID, as reported in the
		 *  most recent CDP message. This object is not instantiated if
		 *  no Appliance VLAN-ID field (TLV) was reported in the most
		 *  recently received CDP message.</P>
		 *  
		 */
		// new NamedSnmpVar(NamedSnmpVar.SNMPGAUGE32, CDP_VLANID, ".1.3.6.1.4.1.9.9.23.1.2.1.1.14", 14),

		/**
		 * <P>The amount of power consumed by remote device, as reported
		 *  in the most recent CDP message. This object is not instantiated
		 *  if no Power Consumption field (TLV) was reported in the most
		 *  recently received CDP message.</P>
		 *  
		 */
		// new NamedSnmpVar(NamedSnmpVar.SNMPGAUGE32, CDP_POWERCONS, ".1.3.6.1.4.1.9.9.23.1.2.1.1.15", 15),

		/**
		 * <P>Indicates the size of the largest datagram that can be
		 *  sent/received by remote device, as reported in the most recent
		 *  CDP message. This object is not instantiated if no MTU field
		 *  (TLV) was reported in the most recently received CDP message.</P>
		 *  
		 */
		// new NamedSnmpVar(NamedSnmpVar.SNMPGAUGE32, CDP_POWERCONS, ".1.3.6.1.4.1.9.9.23.1.2.1.1.16", 16),

		/**
		 * <P>Indicates the value of the remote device's sysName MIB object.
		 *  By convention, it is the device's fully qualified domain name.
		 *  This object is not instantiated if no sysName field (TLV) was
		 *  reported in the most recently received CDP message.</P>
		 *  
		 */
		// new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, CDP_SYSNAME, ".1.3.6.1.4.1.9.9.23.1.2.1.1.17", 17),

		/**
		 * <P>Indicates the value of the remote device's sysObjectID MIB
		 *  object. This object is not instantiated if no sysObjectID field
		 *  (TLV) was reported in the most recently received CDP message.</P>
		 *  
		 */
		// new NamedSnmpVar(NamedSnmpVar.SNMPOBJECTID, CDP_SYSOBJID, ".1.3.6.1.4.1.9.9.23.1.2.1.1.18", 18),

		/**
		 * <P>An indication of the type of address contained in the
		 *  corresponding instance of cdpCachePrimaryMgmtAddress.</P>
		 *  
		 */
		// new NamedSnmpVar(NamedSnmpVar.SNMPINT32, CDP_PRIMARYMGMTADDR_TYPE, ".1.3.6.1.4.1.9.9.23.1.2.1.1.19", 19),

		/**
		 * <P>This object indicates the (first) network layer address at
		 *  which the device will accept SNMP messages as reported in the
		 *  most recently received CDP message. If the corresponding
		 *  instance of cdpCachePrimaryMgmtAddrType has the value 'ip(1)',
		 *  then this object would be an IP-address. If the remote device
		 *  is not currently manageable via any network protocol, this
		 *  object has the special value of the IPv4 address 0.0.0.0.
		 *  If the most recently received CDP message did not contain any
		 *  primary address at which the device prefers to receive
		 *  SNMP messages, then this object is not instanstiated.</P>
		 *  
		 */
		// new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, CDP_PRIMARYMGMTADDR, ".1.3.6.1.4.1.9.9.23.1.2.1.1.20", 20),

		/**
		 * <P>An indication of the type of address contained in the
		 *  corresponding instance of cdpCacheSecondryMgmtAddress.</P>
		 *  
		 */
		// new NamedSnmpVar(NamedSnmpVar.SNMPINT32, CDP_SECONDARYMGMTADDR_TYPE, ".1.3.6.1.4.1.9.9.23.1.2.1.1.21", 21),

		/**
		 * <P>This object indicates the alternate network layer address
		 *  (other than the one indicated by cdpCachePrimaryMgmtAddr) at
		 *  which the device will accept SNMP messages as reported in the
		 *  most recently received CDP message. If the corresponding
		 *  instance of cdpCacheSecondaryMgmtAddrType has the value 'ip(1)',
		 *  then this object would be an IP-address. If the most recently
		 *  received CDP message did not contain such an alternate network
		 *  layer address, then this object is not instanstiated.</P>
		 *  
		 */
		// new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, CDP_SECONDARYMGMTADDR, ".1.3.6.1.4.1.9.9.23.1.2.1.1.22", 22),

		/**
		 * <P>Indicates the physical location, as reported by the most recent
		 *  CDP message, of a connector which is on, or physically connected
		 *  to, the remote device's interface over which the CDP packet is
		 *  sent. This object is not instantiated if no Physical Location
		 *  field (TLV) was reported by the most recently received CDP
		 *  message.</P>
		 *  
		 */
		// new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, CDP_PHYSLOC, ".1.3.6.1.4.1.9.9.23.1.2.1.1.23", 23),

		/**
		 * <P>Indicates the time when this cache entry was last changed.
		 *  This object is initialised to the current time when the entry
		 *  gets created and updated to the current time whenever the value
		 *  of any (other) object instance in the corresponding row is
		 *  modified.</P>
		 *  
		 */
		// new NamedSnmpVar(NamedSnmpVar.SNMPTIMETICKS, CDP_LASTCHANGE, ".1.3.6.1.4.1.9.9.23.1.2.1.1.24", 24)
	};

	/**
	 * <P>The TABLE_OID is the object identifier that represents
	 * the root of the table CdPCacheTable in the MIB forest.</P>
	 */
	public static final String TABLE_OID = ".1.3.6.1.4.1.9.9.23.1.2.1.1"; // start of table (GETNEXT)

	/**
	 * <p>Constructor for CdpCacheTableEntry.</p>
	 */
	public CdpCacheTableEntry() {
		super(cdpCache_elemList);
	}


	/** {@inheritDoc} */
	@Override
	public void storeResult(SnmpResult res) {
		if (!hasIfIndex) {
			int ifindex = res.getInstance().getSubIdAt(res.getInstance().length()-2);
			super.storeResult(new SnmpResult(SnmpObjId.get(CDP_IFINDEX_OID), res.getInstance(), 
						SnmpUtils.getValueFactory().getInt32(ifindex)));
			int deviceIndex = res.getInstance().getLastSubId();
                        super.storeResult(new SnmpResult(SnmpObjId.get(CDP_DEVICEINDEX_OID), res.getInstance(), 
                                                         SnmpUtils.getValueFactory().getInt32(deviceIndex)));
			hasIfIndex = true;
		}
		super.storeResult(res);
	}
	
	/**
	 * <p>getCdpCacheIfIndex</p>
	 *
	 * @return a int.
	 */
	public int getCdpCacheIfIndex() {
	    return getInt32(CdpCacheTableEntry.CDP_IFINDEX);
	}
	
	/**
	 * <p>getCdpCacheDeviceIndex</p>
	 *
	 * @return a int.
	 */
	public int getCdpCacheDeviceIndex() {
	    return getInt32(CdpCacheTableEntry.CDP_DEVICEINDEX);
	}

	/**
	 * <p>getCdpCacheAddressType</p>
	 *
	 * @return a int.
	 */
	public int getCdpCacheAddressType() {
	    return getInt32(CdpCacheTableEntry.CDP_ADDRESS_TYPE);
	}
	
	/**
	 * <p>getCdpCacheAddress</p>
	 *
	 * @return a {@link java.net.InetAddress} object.
	 */
	public String getCdpCacheAddress() {
	    return getHexString(CdpCacheTableEntry.CDP_ADDRESS);
	}

	public InetAddress getCdpCacheIpv4Address() {
            return getIpAddressByHexString(getCdpCacheAddress());	    
	}
	
	/**
	 * <p>getCdpCacheVersion</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCdpCacheVersion() {
		return getDisplayString(CdpCacheTableEntry.CDP_VERSION);
	}
	
	/**
	 * <p>getCdpCacheDeviceId</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCdpCacheDeviceId() {
		return getDisplayString(CdpCacheTableEntry.CDP_DEVICEID);
	}
	
	/**
	 * <p>getCdpCacheDevicePort</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCdpCacheDevicePort() {
		return 	getDisplayString(CdpCacheTableEntry.CDP_DEVICEPORT);
	}

	/**
	 * <p>getCdpPlatform</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	//public String getCdpPlatform() {
	//	return 	getDisplayString(CdpCacheTableEntry.CDP_PLATFORM);
	//}

	/**
	 * TODO: Move to {@link InetAddressUtils}?
	 */
	private static InetAddress getIpAddressByHexString(String ipaddrhexstrng) {

		long ipAddr = Long.parseLong(ipaddrhexstrng, 16);
		byte[] bytes = new byte[4];
		bytes[3] = (byte) (ipAddr & 0xff);
		bytes[2] = (byte) ((ipAddr >> 8) & 0xff);
		bytes[1] = (byte) ((ipAddr >> 16) & 0xff);
		bytes[0] = (byte) ((ipAddr >> 24) & 0xff);

		return InetAddressUtils.getInetAddress(bytes);
	}


}
