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

import org.opennms.netmgt.enlinkd.model.IsIsElement.IsisAdminState;
import org.opennms.netmgt.enlinkd.model.IsIsLink;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.TableTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsisCircTableTracker extends TableTracker {
	private final static Logger LOG = LoggerFactory.getLogger(IsisCircTableTracker.class);

    public final static SnmpObjId ISIS_CIRC_IFINDEX_OID = SnmpObjId.get(".1.3.6.1.2.1.138.1.3.2.1.2");
    public final static SnmpObjId ISIS_CIRC_ADMIN_STATE_OID = SnmpObjId.get(".1.3.6.1.2.1.138.1.3.2.1.3");

    public final static String ISIS_CIRC_IFINDEX = "isisCircIfIndex";
    public final static String ISIS_CIRC_ADMIN_STATE = "isisCircAdminState";
    public static final SnmpObjId[] isisCirctable_elemList = new SnmpObjId[] {
        /*
         *  isisCircIfIndex OBJECT-TYPE
         *      SYNTAX InterfaceIndex
         *      MAX-ACCESS read-create
         *      STATUS current
         *      DESCRIPTION
         *         "The value of ifIndex for the interface to which this
         *         circuit corresponds.  This object cannot be modified
         *         after creation."
         *  ::= { isisCircEntry 2 }    
         */
            ISIS_CIRC_IFINDEX_OID,
    	
    	/*
    	 *  isisCircAdminState OBJECT-TYPE
         *      SYNTAX IsisAdminState
         *      MAX-ACCESS read-create
         *      STATUS current
         *      DESCRIPTION
         *         "The administrative state of the circuit."
         *      DEFVAL { off }
         *  ::= { isisCircEntry 3 }
    	 */
            ISIS_CIRC_ADMIN_STATE_OID

    };
    
    public static class IsIsCircRow extends SnmpRowResult {

		public IsIsCircRow(int columnCount, SnmpInstId instance) {
			super(columnCount, instance);
            LOG.debug( "column count = {}, instance = {}", columnCount, instance);
		}

	    public Integer getIsisCircIndex() {
	        return getInstance().getLastSubId();
	    }
	    
	    public Integer getIsisCircIfIndex() {
	        return getValue(ISIS_CIRC_IFINDEX_OID).toInt();
	    }
	    
	    public Integer getIsisCircAdminState() {
	        return getValue(ISIS_CIRC_ADMIN_STATE_OID).toInt();
	    }
	    
	    public IsIsLink getIsisLink() {
	        LOG.debug( "getIsisLink: row count: {}", getColumnCount());
                IsIsLink link = new IsIsLink();
    		link.setIsisCircIndex(getIsisCircIndex());
                link.setIsisCircIfIndex(getIsisCircIfIndex());
                link.setIsisCircAdminState(IsisAdminState.get(getIsisCircAdminState()));
                LOG.debug( "getIsisLink:Circ Index: {}, IS-IS Circ If Index: {}, Circ Admin State: {}",
                       link.getIsisCircIndex(),
                       link.getIsisCircIfIndex(),
                       IsisAdminState.getTypeString(getIsisCircAdminState()));
	    	return link;
	    }
    }        

    public IsisCircTableTracker() {
        super(isisCirctable_elemList);
    }

    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new IsIsCircRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processIsisCircRow((IsIsCircRow)row);
    }

    /**
     * <p>processIsisAdjRow</p>
     *
     * @param row a {@link org.opennms.netmgt.enlinkd.snmp.IsisCircTableTracker.IsIsCircRow} object.
     */
    public void processIsisCircRow(final IsIsCircRow row) {
        System.out.printf("\t\t%s (%s)= %s \n", ISIS_CIRC_IFINDEX_OID + "." + row.getInstance().toString(), ISIS_CIRC_IFINDEX, row.getIsisCircIfIndex());
        System.out.printf("\t\t%s (%s)= %s (%s)\n", ISIS_CIRC_ADMIN_STATE_OID + "." + row.getInstance().toString(), ISIS_CIRC_ADMIN_STATE, row.getIsisCircAdminState(), IsisAdminState.getTypeString(row.getIsisCircAdminState()));
    }


}
