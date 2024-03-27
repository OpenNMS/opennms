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

import static org.opennms.core.utils.InetAddressUtils.isValidBridgeAddress;
import static org.opennms.core.utils.InetAddressUtils.normalizeMacAddress;
import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;

import org.opennms.netmgt.enlinkd.model.IpNetToMedia;
import org.opennms.netmgt.enlinkd.model.IpNetToMedia.IpNetToMediaType;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TableTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *<P>The IpNetToMediaTableEntry class is designed to hold all the MIB-II
 * information for one entry in the ipNetToMediaTable. The table effectively
 * contains a list of these entries, each entry having information
 * about one physical address. The entry contains the ifindex binding, the MAC address,
 * ip address and entry type.</P>
 *
 * <P>This object is used by the IpNetToMediaTable to hold information
 * single entries in the table. See the IpNetToMediaTable documentation
 * form more information.</P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio</A>
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 * @version $Id: $
 */
public class IpNetToMediaTableTracker extends TableTracker
{
	private final static Logger LOG = LoggerFactory.getLogger(IpNetToMediaTableTracker.class);
	/**
     * <P>The TABLE_OID is the object identifier that represents
     * the root of the IP Address table in the MIB forest.</P>
     */
    public static final SnmpObjId IPNETTOMEDIA_TABLE_ENTRY_OID = SnmpObjId.get(".1.3.6.1.2.1.4.22.1");    // start of table (GETNEXT)
	// Lookup strings for specific table entries
	//
	public final static	SnmpObjId IPNETTOMEDIA_TABLE_IFINDEX_OID = SnmpObjId.get(IPNETTOMEDIA_TABLE_ENTRY_OID, "1");
	public final static	SnmpObjId IPNETTOMEDIA_TABLE_PHYSADDR_OID = SnmpObjId.get(IPNETTOMEDIA_TABLE_ENTRY_OID, "2");
	public final static	SnmpObjId IPNETTOMEDIA_TABLE_NETADDR_OID = SnmpObjId.get(IPNETTOMEDIA_TABLE_ENTRY_OID, "3");
	public final static	SnmpObjId IPNETTOMEDIA_TABLE_TYPE_OID = SnmpObjId.get(IPNETTOMEDIA_TABLE_ENTRY_OID, "4");

	public final static String IPNETTOMEDIA_TABLE_IFINDEX = "ipNetToMediaIfIndex";
	public final static String IPNETTOMEDIA_TABLE_PHYSADDR = "ipNetToMediaPhysAddress";
	public final static String IPNETTOMEDIA_TABLE_NETADDR = "ipNetToMediaNetAddress";
	public final static String IPNETTOMEDIA_TABLE_TYPE = "ipNetToMediaType";
	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the IpNetToMediatable. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	public static SnmpObjId[] ms_elemList = new SnmpObjId[] {
			IPNETTOMEDIA_TABLE_IFINDEX_OID,
		/*
         * The media-dependent `physical' address. 
         */
			IPNETTOMEDIA_TABLE_PHYSADDR_OID,

		/*
         * The IpAddress corresponding to the media-
         * dependent `physical' address.
         */
			IPNETTOMEDIA_TABLE_NETADDR_OID,
        
		/*
		 * ipNetToMediaType OBJECT-TYPE
     	 * SYNTAX      INTEGER {
         *       other(1),        -- none of the following
         *       invalid(2),      -- an invalidated mapping
         *       dynamic(3),
         *       static(4)
         *   }
    	 *	MAX-ACCESS  read-create
    	 *	STATUS      current
    	 *	DESCRIPTION
         *   "The type of mapping.
         *   Setting this object to the value invalid(2) has the effect
         *   of invalidating the corresponding entry in the
         *   ipNetToMediaTable.  That is, it effectively disassociates
         *   the interface identified with said entry from the mapping
         *   identified with said entry.  It is an implementation-
         *   specific matter as to whether the agent removes an
         *   invalidated entry from the table.  Accordingly, management
         *   stations must be prepared to receive tabular information
         *   from agents that corresponds to entries not currently in
         *   use.  Proper interpretation of such entries requires
         *   examination of the relevant ipNetToMediaType object."         
         */
			IPNETTOMEDIA_TABLE_TYPE_OID
		};

	public static class IpNetToMediaRow extends SnmpRowResult {

		public IpNetToMediaRow(int columnCount, SnmpInstId instance) {
			super(columnCount, instance);
		}
		
		public String getIpNetToMediaPhysAddress(){
		    SnmpValue mac = getValue(IPNETTOMEDIA_TABLE_PHYSADDR_OID);
		    if ( mac == null ) {
		        return null;
		    }
		    // Try to fetch the physical address value as a hex string.
	            String hexString = mac.toHexString();
	            LOG.debug("getIpNetToMediaPhysAddress: checking as hexString {}", hexString);
	            if (isValidBridgeAddress(hexString)) {
	                // If the hex string is 12 characters long, than the agent is kinda weird and
	                // is returning the value as a raw binary value that is 6 bytes in length.
	                // But that's OK, as long as we can convert it into a string, that's fine. 
	                return hexString;
	            }
	            try{ 
	                if (mac.isDisplayable()) {
	                // This is the normal case that most agents conform to: the value is an ASCII 
	                // string representing the colon-separated MAC address. We just need to reformat 
	                // it to remove the colons and convert it into a 12-character string.
	                    String displayString = mac.toDisplayString();
	                    return displayString == null || displayString.trim().isEmpty() ? null : normalizeMacAddress(displayString);
	                }
		    } catch (IllegalArgumentException e) {
		        LOG.warn("getIpNetToMediaPhysAddress: IllegalArgument mac on ipnettomediatable:  return null", e);
		        return null;
		    }
	            if (hexString != null && !hexString.trim().isEmpty() && isValidBridgeAddress(hexString))
	                return hexString;
                    LOG.warn("getIpNetToMediaPhysAddress: not valid mac {}, return null", hexString);
	            return null;
		}
		
		/**
		 * <p>getIpNetToMediaNetAddress</p>
		 *
		 * @return a {@link java.net.InetAddress} object.
		 */
		public InetAddress getIpNetToMediaNetAddress(){
		    SnmpValue value = getValue(IPNETTOMEDIA_TABLE_NETADDR_OID);
                    if (value == null) {
                        return null;
                    }		    
		    return value.toInetAddress();
		}
		
		/**
		 * <p>getIpNetToMediatype</p>
		 *
		 * @return a int.
		 */
		public Integer getIpNetToMediatype(){
		    SnmpValue value = getValue(IPNETTOMEDIA_TABLE_TYPE_OID);
		    if (value == null) {
		        return null;
		    }
		    return value.toInt();
		}
		
		public Integer getIpNetToMediaIfIndex() {
		    SnmpValue value = getValue(IPNETTOMEDIA_TABLE_IFINDEX_OID);
                    if (value == null) {
                        return null;
                    }
		    return value.toInt();
		}
		
		public IpNetToMedia getIpNetToMedia() {
			IpNetToMedia at = new IpNetToMedia();
			at.setSourceIfIndex(getIpNetToMediaIfIndex());
			at.setPhysAddress(getIpNetToMediaPhysAddress());
			at.setNetAddress(getIpNetToMediaNetAddress());
			at.setIpNetToMediaType(IpNetToMediaType.get(getIpNetToMediatype()));
			return at;
		}
	}
	/**
	 * <P>Creates a default instance of the ipNetToMediatableTracker
	 * table entry map. The map represents a singular
	 * instance of the mac address table. Each column in
	 * the table for the loaded instance may be retrieved
	 * either through its name or object identifier.</P>
	 *
	 * <P>The initial table is constructed with zero
	 * elements in the map.</P>
	 */
	public IpNetToMediaTableTracker( )
	{
		super(ms_elemList);
	}

    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new IpNetToMediaRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processIpNetToMediaRow((IpNetToMediaRow)row);
    }

    /**
     * <p>processIpNetToMediaRow</p>
     *
     * @param row a {@link org.opennms.netmgt.enlinkd.snmp.IpNetToMediaTableTracker.IpNetToMediaRow} object.
     */
    public void processIpNetToMediaRow(final IpNetToMediaRow row) {
		System.out.printf("\t\t%s (%s)= %s \n", IPNETTOMEDIA_TABLE_IFINDEX_OID + "." + row.getInstance().toString(), IPNETTOMEDIA_TABLE_IFINDEX, row.getIpNetToMediaIfIndex());
		System.out.printf("\t\t%s (%s)= %s \n", IPNETTOMEDIA_TABLE_PHYSADDR_OID + "." + row.getInstance().toString(), IPNETTOMEDIA_TABLE_PHYSADDR, row.getIpNetToMediaPhysAddress());
		System.out.printf("\t\t%s (%s)= %s \n", IPNETTOMEDIA_TABLE_NETADDR_OID + "." + row.getInstance().toString(), IPNETTOMEDIA_TABLE_NETADDR, str(row.getIpNetToMediaNetAddress()));
		System.out.printf("\t\t%s (%s)= %s (%s)\n", IPNETTOMEDIA_TABLE_TYPE_OID + "." + row.getInstance().toString(), IPNETTOMEDIA_TABLE_TYPE, row.getIpNetToMediatype(), IpNetToMediaType.get(row.getIpNetToMediatype()) );

	}

}   
