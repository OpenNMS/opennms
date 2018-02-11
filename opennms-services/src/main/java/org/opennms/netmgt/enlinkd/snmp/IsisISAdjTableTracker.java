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

import org.opennms.netmgt.model.IsIsLink;
import org.opennms.netmgt.model.IsIsLink.IsisISAdjNeighSysType;
import org.opennms.netmgt.model.IsIsLink.IsisISAdjState;
import org.opennms.netmgt.snmp.RowCallback;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.TableTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsisISAdjTableTracker extends TableTracker {
	private final static Logger LOG = LoggerFactory.getLogger(IsisISAdjTableTracker.class);

    public static final SnmpObjId ISIS_IS_ADJ_TABLE = SnmpObjId.get(".1.3.6.1.2.1.138.1.6.1"); // start of table (GETNEXT)

    public final static SnmpObjId ISIS_IS_ADJ_STATE                = SnmpObjId.get(".1.3.6.1.2.1.138.1.6.1.1.2");
    public final static SnmpObjId ISIS_IS_ADJ_NBR_SNPAADDR         = SnmpObjId.get(".1.3.6.1.2.1.138.1.6.1.1.4");
    public final static SnmpObjId ISIS_IS_ADJ_NBR_SYSTYPE          = SnmpObjId.get(".1.3.6.1.2.1.138.1.6.1.1.5");
    public final static SnmpObjId ISIS_IS_ADJ_NBR_SYSID            = SnmpObjId.get(".1.3.6.1.2.1.138.1.6.1.1.6");
    public final static SnmpObjId ISIS_IS_ADJ_NBR_EXTENDED_CIRCID  = SnmpObjId.get(".1.3.6.1.2.1.138.1.6.1.1.7");

    public static final SnmpObjId[] isisIsAdjtable_elemList = new SnmpObjId[] {
        
        /**
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
        ISIS_IS_ADJ_STATE,

        /**
         *  isisISAdjNeighSNPAAddress OBJECT-TYPE
         *  SYNTAX IsisOSINSAddress
         *  MAX-ACCESS read-only
         *  STATUS current
         *  DESCRIPTION
         *  "The SNPA address of the neighboring system."
         *  REFERENCE "{ISIS.aoi neighbourSNPAAddress (79)}"
         *  ::= { isisISAdjEntry 4 }
         */
        ISIS_IS_ADJ_NBR_SNPAADDR,
        
        /**
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
        ISIS_IS_ADJ_NBR_SYSTYPE,
        
        /**
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
         ISIS_IS_ADJ_NBR_SYSID,
        
        /**
         * isisISAdjNbrExtendedCircID OBJECT-TYPE
         * SYNTAX Unsigned32
         * MAX-ACCESS read-only
         * STATUS current
         * DESCRIPTION
         *  "The 4-byte Extended Circuit ID learned from the
         *    Neighbor during 3-way handshake, or 0."
         *    ::= { isisISAdjEntry 7 }
         */
        ISIS_IS_ADJ_NBR_EXTENDED_CIRCID

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
	    	return getValue(ISIS_IS_ADJ_STATE).toInt();
	    }

	    public String getIsisISAdjNeighSnpaAddress() {
	        return getValue(ISIS_IS_ADJ_NBR_SNPAADDR).toHexString();
	    }
	    
	    public Integer getIsisISAdjNeighSysType() {
	        return getValue(ISIS_IS_ADJ_NBR_SYSTYPE).toInt();
	    }
	    
	    public String getIsisISAdjNeighSysID() {
	        return getValue(ISIS_IS_ADJ_NBR_SYSID).toHexString();
	    }
	    
	    public Integer getIsisISAdjNbrExtendedCircID() {
	        return getValue(ISIS_IS_ADJ_NBR_EXTENDED_CIRCID).toInt();
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

    public IsisISAdjTableTracker(final RowCallback rowProcessor) {
        super(rowProcessor,isisIsAdjtable_elemList);
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
    }


}
