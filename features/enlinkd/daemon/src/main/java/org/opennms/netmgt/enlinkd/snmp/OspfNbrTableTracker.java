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

import org.opennms.netmgt.model.OspfLink;
import org.opennms.netmgt.snmp.RowCallback;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.TableTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.opennms.core.utils.InetAddressUtils.str;

public class OspfNbrTableTracker extends TableTracker {
	private final static Logger LOG = LoggerFactory.getLogger(OspfNbrTableTracker.class);

	public final static SnmpObjId OSPF_NBR_IPADDRESS          = SnmpObjId.get(".1.3.6.1.2.1.14.10.1.1");
    public final static SnmpObjId OSPF_NBR_ADDRESS_LESS_INDEX = SnmpObjId.get(".1.3.6.1.2.1.14.10.1.2");
    public final static SnmpObjId OSPF_NBR_ROUTERID           = SnmpObjId.get(".1.3.6.1.2.1.14.10.1.3");
 
    public static final SnmpObjId[] s_ospfnbrtable_elemList = new SnmpObjId[] {
        
        /**
         * <p>
         * "The IP address this neighbor is using  in  its
         * IP  Source  Address.  Note that, on addressless
         * links, this will not be 0.0.0.0,  but  the  ad-
         * dress of another of the neighbor's interfaces."
         * </p>
        */
        OSPF_NBR_IPADDRESS,
        
        /**
         * <p>
         * "On an interface having an  IP  Address,  zero.
         * On  addressless  interfaces,  the corresponding
         * value of ifIndex in the Internet Standard  MIB.
         * On  row  creation, this can be derived from the
         * instance."
         * </p>
         */
        OSPF_NBR_ADDRESS_LESS_INDEX,

        /**
         * <p>
         * "A 32-bit integer (represented as a type  IpAd-
         * dress)  uniquely  identifying  the  neighboring
         * router in the Autonomous System."
         * DEFVAL   { '00000000'H }    -- 0.0.0.0
         * </p>
         */
        OSPF_NBR_ROUTERID
    };
        
    public static class OspfNbrRow extends SnmpRowResult {
    	
        public OspfNbrRow(int columnCount, SnmpInstId instance) {
			super(columnCount, instance);
		}

		public InetAddress getOspfNbrIpAddress() {
			return getValue(OSPF_NBR_IPADDRESS).toInetAddress();
		}

	    public InetAddress getOspfNbrRouterId() {
	        return getValue(OSPF_NBR_ROUTERID).toInetAddress();
	    }
	
	    public Integer getOspfNbrAddressLessIndex() {
	        return getValue(OSPF_NBR_ADDRESS_LESS_INDEX).toInt();
	    }

		public OspfLink getOspfLink() {
            LOG.debug( "getOspfLink: row count: {}", getColumnCount());
            OspfLink link = new OspfLink();
            LOG.debug( "getOspfLink: nbr router id: {}, nbr ip address {}, nbr address less ifindex {}", 
                       str(getOspfNbrRouterId()),
                       str(getOspfNbrIpAddress()),
                       getOspfNbrAddressLessIndex());
            link.setOspfRemRouterId(getOspfNbrRouterId());
            link.setOspfRemIpAddr(getOspfNbrIpAddress());
            link.setOspfRemAddressLessIndex(getOspfNbrAddressLessIndex());
			return link;
		}
	    
	}
    
    public OspfNbrTableTracker() {
        super(s_ospfnbrtable_elemList);
    }

    public OspfNbrTableTracker(RowCallback rowProcessor) {
    	super(rowProcessor,s_ospfnbrtable_elemList);
    }

    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new OspfNbrRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processOspfNbrRow((OspfNbrRow)row);
    }

    /**
     * <p>processOspfIfRow</p>
     *
     * @param row a {@link org.opennms.netmgt.enlinkd.snmp.OspfNbrTableTracker.OspfNbrRow} object.
     */
    public void processOspfNbrRow(final OspfNbrRow row) {
    }



}
