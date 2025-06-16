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

import java.net.InetAddress;

import org.opennms.netmgt.enlinkd.model.OspfLink;
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

	public final static SnmpObjId OSPF_NBR_IPADDRESS_OID = SnmpObjId.get(".1.3.6.1.2.1.14.10.1.1");
    public final static SnmpObjId OSPF_NBR_ADDRESS_LESS_INDEX_OID = SnmpObjId.get(".1.3.6.1.2.1.14.10.1.2");
    public final static SnmpObjId OSPF_NBR_ROUTERID_OID = SnmpObjId.get(".1.3.6.1.2.1.14.10.1.3");

    public final static String OSPF_NBR_IPADDRESS = "ospfNbrIpAddr";
    public final static String OSPF_NBR_ADDRESS_LESS_INDEX = "ospfNbrAddressLessIndex";
    public final static String OSPF_NBR_ROUTERID = "ospfNbrRtrId";
    public static final SnmpObjId[] s_ospfnbrtable_elemList = new SnmpObjId[] {
        
        /*
         * <p>
         * "The IP address this neighbor is using  in  its
         * IP  Source  Address.  Note that, on addressless
         * links, this will not be 0.0.0.0,  but  the  ad-
         * dress of another of the neighbor's interfaces."
         * </p>
        */
            OSPF_NBR_IPADDRESS_OID,
        
        /*
         * <p>
         * "On an interface having an  IP  Address,  zero.
         * On  addressless  interfaces,  the corresponding
         * value of ifIndex in the Internet Standard  MIB.
         * On  row  creation, this can be derived from the
         * instance."
         * </p>
         */
            OSPF_NBR_ADDRESS_LESS_INDEX_OID,

        /*
         * <p>
         * "A 32-bit integer (represented as a type  IpAd-
         * dress)  uniquely  identifying  the  neighboring
         * router in the Autonomous System."
         * DEFVAL   { '00000000'H }    -- 0.0.0.0
         * </p>
         */
            OSPF_NBR_ROUTERID_OID
    };
        
    public static class OspfNbrRow extends SnmpRowResult {
    	
        public OspfNbrRow(int columnCount, SnmpInstId instance) {
			super(columnCount, instance);
		}

		public InetAddress getOspfNbrIpAddress() {
			return getValue(OSPF_NBR_IPADDRESS_OID).toInetAddress();
		}

	    public InetAddress getOspfNbrRouterId() {
	        return getValue(OSPF_NBR_ROUTERID_OID).toInetAddress();
	    }
	
	    public Integer getOspfNbrAddressLessIndex() {
	        return getValue(OSPF_NBR_ADDRESS_LESS_INDEX_OID).toInt();
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
        System.out.printf("\t\t%s (%s)= %s \n", OSPF_NBR_IPADDRESS_OID + "." + row.getInstance().toString(), OSPF_NBR_IPADDRESS, str(row.getOspfNbrIpAddress()));
        System.out.printf("\t\t%s (%s)= %s \n", OSPF_NBR_ADDRESS_LESS_INDEX_OID + "." + row.getInstance().toString(), OSPF_NBR_ADDRESS_LESS_INDEX, row.getOspfNbrAddressLessIndex());
        System.out.printf("\t\t%s (%s)= %s \n", OSPF_NBR_ROUTERID_OID + "." + row.getInstance().toString(), OSPF_NBR_ROUTERID, str(row.getOspfNbrRouterId()));
    }



}
