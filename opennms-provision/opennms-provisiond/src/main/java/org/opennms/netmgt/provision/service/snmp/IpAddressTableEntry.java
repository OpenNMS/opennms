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
package org.opennms.netmgt.provision.service.snmp;

import static org.opennms.core.utils.InetAddressUtils.getInetAddress;

import java.net.InetAddress;

import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.provision.service.IPAddressTableTracker;
import org.opennms.netmgt.snmp.NamedSnmpVar;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;

/**
 * <P>
 * The IpAddrTableEntry class is designed to hold all the MIB-II information for
 * one entry in the ipAddrTable. The table effectively contains a list of these
 * entries, each entry having information about one address. The entry contains
 * an IP Address, its netmask, interface binding, broadcast address, and maximum
 * packet reassembly size.
 * </P>
 *
 * <P>
 * This object is used by the IpAddrTable to hold information single entries in
 * the table. See the IpAddrTable documentation form more information.
 * </P>
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A>Jon Whetzel </A>
 * @see IpAddrTable
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213 </A>
 */
public final class IpAddressTableEntry extends SnmpTableEntry {
    private static final Logger LOG = LoggerFactory.getLogger(IpAddressTableEntry.class);
    // Lookup strings for specific table entries

    public static final String IP_ADDRESS_IF_INDEX = "ipAddressIfIndex";
    public static final String IP_ADDR_ENT_NETMASK = "ipAddressPrefix";
    public static final String IP_ADDR_TYPE        = "ipAddressType";

    /**
     * <P>
     * The TABLE_OID is the object identifier that represents the root of the IP
     * Address table in the MIB forest.
     * </P>
     */
    public static final String TABLE_OID = "..1.3.6.1.2.1.4.34.1";

    /** Constant <code>ms_elemList</code> */
    public static NamedSnmpVar[] ms_elemList = new NamedSnmpVar[] {
    	new NamedSnmpVar(NamedSnmpVar.SNMPOBJECTID, IP_ADDRESS_IF_INDEX, TABLE_OID + ".3", 1),
    	new NamedSnmpVar(NamedSnmpVar.SNMPOBJECTID, IP_ADDR_ENT_NETMASK, TABLE_OID + ".5", 2),
    	new NamedSnmpVar(NamedSnmpVar.SNMPOBJECTID, IP_ADDR_TYPE,        TABLE_OID + ".4", 3)
    };

    private InetAddress m_inetAddress = null;

    /**
     * <P>
     * The class constructor used to initialize the object to its initial state.
     * Although the object's member variables can change after an instance is
     * created, this constructor will initialize all the variables as per their
     * named variable from the passed array of SNMP varbinds.
     * </P>
     *
     * <P>
     * If the information in the object should not be modified then a <EM>final
     * </EM> modifier can be applied to the created object.
     * </P>
     */
    public IpAddressTableEntry() {
        super(ms_elemList);
    }

    /**
     * <p>getIpAdEntAddr</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getIpAddress() {
    	return m_inetAddress;
    }

    /**
     * <p>getIpAdEntIfIndex</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getIpAddressIfIndex() {
        return getInt32(IpAddressTableEntry.IP_ADDRESS_IF_INDEX);
    }

    /**
     * <p>getIpAdEntNetMask</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getIpAddressNetMask() {
    	final SnmpValue value = getValue(IP_ADDR_ENT_NETMASK);
    	// LOG.debug("getIpAddressNetMask: value = {}", value.toDisplayString());
    	final SnmpObjId netmaskRef = value.toSnmpObjId().getInstance(IPAddressTableTracker.IP_ADDRESS_PREFIX_ORIGIN_INDEX);

    	if (netmaskRef == null) {
    	    LOG.warn("Unable to get netmask reference from instance.");
    	    return null;
    	}

    	final int[] rawIds = netmaskRef.getIds();
    	final int addressType = rawIds[1];
    	final int addressLength = rawIds[2];
    	final InetAddress address = getInetAddress(rawIds, 3, addressLength);
    	final int mask = rawIds[rawIds.length - 1];

    	if (addressType == IPAddressTableTracker.TYPE_IPV4) {
    	    return InetAddressUtils.convertCidrToInetAddressV4(mask);
    	} else if (addressType == IPAddressTableTracker.TYPE_IPV6) {
    	    return InetAddressUtils.convertCidrToInetAddressV6(mask);
    	} else if (addressType == IPAddressTableTracker.TYPE_IPV6Z) {
    	    LOG.debug("Got an IPv6z address, returning {}", address);
    	} else {
    	    LOG.warn("Unsure how to handle IP address type ({})", addressType);
    	}
        return address;
    }
    
    /**
     * This is a hack, we get the IP address from the instance information when storing one of the columns.  :P
     */
    @Override
    public void storeResult(final SnmpResult result) {
    	final int[] instanceIds = result.getInstance().getIds();
    	final int addressType = instanceIds[1];
		if (addressType == IPAddressTableTracker.TYPE_IPV4 || addressType == IPAddressTableTracker.TYPE_IPV6 || addressType == IPAddressTableTracker.TYPE_IPV6Z) {
			m_inetAddress = InetAddressUtils.getInetAddress(instanceIds, 2, addressType);
		} else {
			LOG.warn("Unable to determine IP address type ({})", addressType);
		}

    	super.storeResult(result);
    }
}
