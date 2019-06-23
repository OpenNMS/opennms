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

import static org.opennms.core.utils.InetAddressUtils.normalizeMacAddress;
import static org.opennms.core.utils.InetAddressUtils.isValidBridgeAddress;

import java.net.InetAddress;

import org.opennms.netmgt.model.IpNetToMedia;
import org.opennms.netmgt.model.IpNetToMedia.IpNetToMediaType;
import org.opennms.netmgt.snmp.RowCallback;
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
 * @see IpNetToMediaTable
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
    public static final SnmpObjId  IPNETTOMEDIA_TABLE_ENTRY   = SnmpObjId.get(".1.3.6.1.2.1.4.22.1");    // start of table (GETNEXT)
	// Lookup strings for specific table entries
	//
	public final static	SnmpObjId	IPNETTOMEDIA_TABLE_IFINDEX	= SnmpObjId.get(IPNETTOMEDIA_TABLE_ENTRY, "1");
	public final static	SnmpObjId	IPNETTOMEDIA_TABLE_PHYSADDR	= SnmpObjId.get(IPNETTOMEDIA_TABLE_ENTRY, "2");
	public final static	SnmpObjId	IPNETTOMEDIA_TABLE_NETADDR	= SnmpObjId.get(IPNETTOMEDIA_TABLE_ENTRY, "3");
	public final static	SnmpObjId	IPNETTOMEDIA_TABLE_TYPE		= SnmpObjId.get(IPNETTOMEDIA_TABLE_ENTRY, "4");

	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the IpNetToMediatable. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	public static SnmpObjId[] ms_elemList = new SnmpObjId[] {
		IPNETTOMEDIA_TABLE_IFINDEX,
		/**
         * The media-dependent `physical' address. 
         */
		IPNETTOMEDIA_TABLE_PHYSADDR,

		/**
         * The IpAddress corresponding to the media-
         * dependent `physical' address.
         */
		IPNETTOMEDIA_TABLE_NETADDR,
        
		/**
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
		IPNETTOMEDIA_TABLE_TYPE
		};

	public static class IpNetToMediaRow extends SnmpRowResult {

		public IpNetToMediaRow(int columnCount, SnmpInstId instance) {
			super(columnCount, instance);
		}
		
		/**
		 * <p>getIpNetToMediaPhysAddress</p>
		 *
		 * @return a {@link java.lang.String} object.
		 * @see {@link org.opennms.netmgt.provision.service.snmp.IfTableEntry#getPhysAddr()}
		 */
		public String getIpNetToMediaPhysAddress(){
		    SnmpValue mac = getValue(IPNETTOMEDIA_TABLE_PHYSADDR);
		    if ( mac == null ) {
		        return null;
		    }
		    // Try to fetch the physical address value as a hex string.
	            String hexString = mac.toHexString();
	            LOG.debug("getIpNetToMediaPhysAddress: checking as hexString {}", hexString);
	            if (hexString != null && 
	                    isValidBridgeAddress(hexString)) {
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
		    SnmpValue value = getValue(IPNETTOMEDIA_TABLE_NETADDR);
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
		    SnmpValue value = getValue(IPNETTOMEDIA_TABLE_TYPE);
		    if (value == null) {
		        return null;
		    }
		    return value.toInt();
		}
		
		public Integer getIpNetToMediaIfIndex() {
		    SnmpValue value = getValue(IPNETTOMEDIA_TABLE_IFINDEX);
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


	public IpNetToMediaTableTracker(RowCallback rowProcessor) {
		super(rowProcessor, ms_elemList);
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
    }

}   
