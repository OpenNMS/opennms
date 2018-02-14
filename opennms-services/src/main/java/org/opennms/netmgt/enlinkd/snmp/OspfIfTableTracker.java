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

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;
import org.opennms.netmgt.model.OspfLink;
import org.opennms.netmgt.snmp.RowCallback;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.TableTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OspfIfTableTracker extends TableTracker {

	private final static Logger LOG = LoggerFactory.getLogger(OspfIfTableTracker.class);
	
    public static final SnmpObjId OSPF_IF_TABLE_ENTRY  = SnmpObjId.get(".1.3.6.1.2.1.14.7.1"); // start of table (GETNEXT)
    
    public final static SnmpObjId OSPF_IF_IPADDRESS    = SnmpObjId.get(".1.3.6.1.2.1.14.7.1.1");
    public final static SnmpObjId OSPF_ADDRESS_LESS_IF = SnmpObjId.get(".1.3.6.1.2.1.14.7.1.2");

    public static final SnmpObjId[] s_ospfiftable_elemList = new SnmpObjId[] {
        
        /**
         *  "The IP address of this OSPF interface."
        */
        OSPF_IF_IPADDRESS,
        
        /**
         * "For the purpose of easing  the  instancing  of
         * addressed   and  addressless  interfaces;  This
         * variable takes the value 0 on  interfaces  with
         * IP  Addresses,  and  the corresponding value of
         * ifIndex for interfaces having no IP Address."
         * 
         */
        OSPF_ADDRESS_LESS_IF
        
    };
    
    public static class OspfIfRow extends SnmpRowResult {

    	public OspfIfRow(int columnCount, SnmpInstId instance) {
			super(columnCount, instance);
            LOG.debug( "column count = {}, instance = {}", columnCount, instance);
		}
    	
    	public InetAddress getOspfIpAddress() {
	        return getValue(OSPF_IF_IPADDRESS).toInetAddress();
	    }
	    
	    public Integer getOspfAddressLessIf() {
	        return getValue(OSPF_ADDRESS_LESS_IF).toInt();
	    }
	    

	public OspfLink getOspfLink() {
		
            LOG.debug( "getOspfLink: ospf ip address: {}, address less ifindex {}", 
                       str(getOspfIpAddress()),
                       getOspfAddressLessIf());

            OspfLink link = new OspfLink();
            link.setOspfIpAddr(getOspfIpAddress());
            link.setOspfAddressLessIndex(getOspfAddressLessIf());
            return link;

	}

    }
    
    

    public OspfIfTableTracker() {
        super(s_ospfiftable_elemList);
    }

    public OspfIfTableTracker(final RowCallback rowProcessor) {
    	super(rowProcessor,s_ospfiftable_elemList);
    }

    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new OspfIfRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processOspfIfRow((OspfIfRow)row);
    }

    /**
     * <p>processOspfIfRow</p>
     *
     * @param row a {@link org.opennms.netmgt.enlinkd.snmp.OspfIfTableTracker.OspfIfRow} object.
     */
    public void processOspfIfRow(final OspfIfRow row) {
    }


}
