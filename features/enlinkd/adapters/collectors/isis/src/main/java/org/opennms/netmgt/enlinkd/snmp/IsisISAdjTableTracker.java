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

import org.opennms.netmgt.enlinkd.model.IsIsElement;
import org.opennms.netmgt.enlinkd.model.IsIsLink;
import org.opennms.netmgt.enlinkd.model.IsIsLink.IsisISAdjNeighSysType;
import org.opennms.netmgt.enlinkd.model.IsIsLink.IsisISAdjState;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.TableTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsisISAdjTableTracker extends TableTracker {
	private final static Logger LOG = LoggerFactory.getLogger(IsisISAdjTableTracker.class);

    public final static SnmpObjId ISIS_IS_ADJ_STATE_OID = SnmpObjId.get(".1.3.6.1.2.1.138.1.6.1.1.2");
    public final static SnmpObjId ISIS_IS_ADJ_NBR_SNPAADDR_OID = SnmpObjId.get(".1.3.6.1.2.1.138.1.6.1.1.4");
    public final static SnmpObjId ISIS_IS_ADJ_NBR_SYSTYPE_OID = SnmpObjId.get(".1.3.6.1.2.1.138.1.6.1.1.5");
    public final static SnmpObjId ISIS_IS_ADJ_NBR_SYSID_OID = SnmpObjId.get(".1.3.6.1.2.1.138.1.6.1.1.6");
    public final static SnmpObjId ISIS_IS_ADJ_NBR_EXTENDED_CIRCID_OID = SnmpObjId.get(".1.3.6.1.2.1.138.1.6.1.1.7");

    public final static String ISIS_IS_ADJ_STATE = "isisISAdjState";
    public final static String ISIS_IS_ADJ_NBR_SNPAADDR = "isisISAdjNeighSNPAAddress";
    public final static String ISIS_IS_ADJ_NBR_SYSTYPE = "isisISAdjNeighSysType";
    public final static String ISIS_IS_ADJ_NBR_SYSID = "isisISAdjNeighSysID";
    public final static String ISIS_IS_ADJ_NBR_EXTENDED_CIRCID = "isisISAdjNbrExtendedCircID";

    public static final SnmpObjId[] isisIsAdjtable_elemList = new SnmpObjId[] {
        
        /*
         * isisISAdjState OBJECT-TYPE
         * SYNTAX INTEGER
         *  {
         *        down (1),
         *        initializing (2),
         *        up (3),
         *        failed(4)
         *  }
         * MAX-ACCESS read-only
         * STATUS current
         * DESCRIPTION
         *   "The state of the adjacency."
         * REFERENCE "{ISIS.aoi adjacencyState (78)}"
         * ::= { isisISAdjEntry 2 }
         */
            ISIS_IS_ADJ_STATE_OID,

        /*
         *  isisISAdjNeighSNPAAddress OBJECT-TYPE
         *  SYNTAX IsisOSINSAddress
         *  MAX-ACCESS read-only
         *  STATUS current
         *  DESCRIPTION
         *  "The SNPA address of the neighboring system."
         *  REFERENCE "{ISIS.aoi neighbourSNPAAddress (79)}"
         *  ::= { isisISAdjEntry 4 }
         */
            ISIS_IS_ADJ_NBR_SNPAADDR_OID,
        
        /*
         *  isisISAdjNeighSysType OBJECT-TYPE
         *          SYNTAX INTEGER
         *          {
         *          l1IntermediateSystem(1),
         *          l2IntermediateSystem(2),
         *          l1L2IntermediateSystem(3),
         *          unknown(4)
         *          }
         *          MAX-ACCESS read-only
         *          STATUS current
         *          DESCRIPTION
         *          "The type of the neighboring system."
         *          REFERENCE "{ISIS.aoi neighbourSystemType (80)}"
         *  ::= { isisISAdjEntry 5 }
         */
            ISIS_IS_ADJ_NBR_SYSTYPE_OID,
        
        /*
         *     isisISAdjNeighSysID OBJECT-TYPE
         *     SYNTAX IsisSystemID
         *     MAX-ACCESS read-only
         *     STATUS current
         *     DESCRIPTION
         *     "The system ID of the neighboring Intermediate
         *     System."
         *     REFERENCE "{ISIS.aoi neighbourSystemIds (83)}"
         *     ::= { isisISAdjEntry 6 }
         */
            ISIS_IS_ADJ_NBR_SYSID_OID,
        
        /*
         * isisISAdjNbrExtendedCircID OBJECT-TYPE
         * SYNTAX Unsigned32
         * MAX-ACCESS read-only
         * STATUS current
         * DESCRIPTION
         *  "The 4-byte Extended Circuit ID learned from the
         *    Neighbor during 3-way handshake, or 0."
         *    ::= { isisISAdjEntry 7 }
         */
            ISIS_IS_ADJ_NBR_EXTENDED_CIRCID_OID

    };
    
    public static class IsIsAdjRow extends SnmpRowResult {
		public IsIsAdjRow(int columnCount, SnmpInstId instance) {
			super(columnCount, instance);
            LOG.debug( "column count = {}, instance = {}", columnCount, instance);
		}
    	
	    public Integer getIsisCircIndex() {
	    	return getInstance().getSubIdAt(0);
	    }

	    public Integer getIsisISAdjIndex() {
	    	return getInstance().getLastSubId();
	    }
	    
	    public Integer getIsisISAdjStatus() {
	    	return getValue(ISIS_IS_ADJ_STATE_OID).toInt();
	    }

	    public String getIsisISAdjNeighSnpaAddress() {
	        return getValue(ISIS_IS_ADJ_NBR_SNPAADDR_OID).toHexString();
	    }
	    
	    public Integer getIsisISAdjNeighSysType() {
	        return getValue(ISIS_IS_ADJ_NBR_SYSTYPE_OID).toInt();
	    }
	    
	    public String getIsisISAdjNeighSysID() {
	        return getValue(ISIS_IS_ADJ_NBR_SYSID_OID).toHexString();
	    }
	    
	    public Integer getIsisISAdjNbrExtendedCircID() {
	        return getValue(ISIS_IS_ADJ_NBR_EXTENDED_CIRCID_OID).toInt();
	    }

	    public IsIsLink getIsisLink() {
            LOG.debug( "getIsisLink: row count: {}", getColumnCount());
            IsIsLink link = new IsIsLink();
            link.setIsisCircIndex(getIsisCircIndex());
            link.setIsisISAdjIndex(getIsisISAdjIndex());
            link.setIsisISAdjState(IsisISAdjState.get(getIsisISAdjStatus()));
            link.setIsisISAdjNeighSNPAAddress(getIsisISAdjNeighSnpaAddress());
            link.setIsisISAdjNeighSysType(IsisISAdjNeighSysType.get(getIsisISAdjNeighSysType()));
            link.setIsisISAdjNeighSysID(getIsisISAdjNeighSysID());
            link.setIsisISAdjNbrExtendedCircID(getIsisISAdjNbrExtendedCircID());
            LOG.debug( "getIsisLink: Circ Index: {}, Adj Index: {}, Adj State: {}, Adj Neigh SNPA Address: {},"
                    + "\n Adj Neigh Sys Type: {}, Adj Neigh Sys ID: {}, Adj Nbr Extended Circ ID: {}",
                      link.getIsisCircIndex(),
                      link.getIsisISAdjIndex(),
                      IsisISAdjState.getTypeString(getIsisISAdjStatus()),
                      link.getIsisISAdjNeighSNPAAddress(),
                      IsisISAdjNeighSysType.getTypeString(getIsisISAdjNeighSysType()),
                      link.getIsisISAdjNeighSysID(),
                      link.getIsisISAdjNbrExtendedCircID());
            return link;
	    }
    }

    public IsisISAdjTableTracker() {
        super(isisIsAdjtable_elemList);
    }

    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new IsIsAdjRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processIsisAdjRow((IsIsAdjRow)row);
    }

    /**
     * <p>processIsisAdjRow</p>
     *
     * @param row a {@link org.opennms.netmgt.enlinkd.snmp.IsisISAdjTableTracker.IsIsAdjRow} object.
     */
    public void processIsisAdjRow(final IsIsAdjRow row) {
        System.out.printf("\t\t%s (%s)= %s (%s)\n", ISIS_IS_ADJ_STATE_OID + "." + row.getInstance().toString(), ISIS_IS_ADJ_STATE, row.getIsisISAdjStatus(), IsisISAdjState.getTypeString(row.getIsisISAdjStatus()));
        System.out.printf("\t\t%s (%s)= %s \n", ISIS_IS_ADJ_NBR_SNPAADDR_OID + "." + row.getInstance().toString(), ISIS_IS_ADJ_NBR_SNPAADDR, row.getIsisISAdjNeighSnpaAddress());
        System.out.printf("\t\t%s (%s)= %s (%s)\n", ISIS_IS_ADJ_NBR_SYSTYPE_OID + "." + row.getInstance().toString(), ISIS_IS_ADJ_NBR_SYSTYPE, row.getIsisISAdjNeighSysType(), IsisISAdjNeighSysType.getTypeString(row.getIsisISAdjNeighSysType()));
        System.out.printf("\t\t%s (%s)= %s \n", ISIS_IS_ADJ_NBR_SYSID_OID + "." + row.getInstance().toString(), ISIS_IS_ADJ_NBR_SYSID, row.getIsisISAdjNeighSysID());
        System.out.printf("\t\t%s (%s)= %s \n", ISIS_IS_ADJ_NBR_EXTENDED_CIRCID_OID + "." + row.getInstance().toString(), ISIS_IS_ADJ_NBR_EXTENDED_CIRCID, row.getIsisISAdjNbrExtendedCircID());
    }


}
