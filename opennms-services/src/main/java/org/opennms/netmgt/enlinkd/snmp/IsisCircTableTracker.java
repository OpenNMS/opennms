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
import org.opennms.netmgt.model.IsIsElement.IsisAdminState;
import org.opennms.netmgt.snmp.RowCallback;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.TableTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsisCircTableTracker extends TableTracker {
	private final static Logger LOG = LoggerFactory.getLogger(IsisCircTableTracker.class);

    public static final SnmpObjId ISIS_CIRC_TABLE       = SnmpObjId.get(".1.3.6.1.2.1.138.1.3.2"); // start of table (GETNEXT)
    public final static SnmpObjId ISIS_CIRC_IFINDEX     = SnmpObjId.get(".1.3.6.1.2.1.138.1.3.2.1.2");
    public final static SnmpObjId ISIS_CIRC_ADMIN_STATE = SnmpObjId.get(".1.3.6.1.2.1.138.1.3.2.1.3");

    public static final SnmpObjId[] isisCirctable_elemList = new SnmpObjId[] {
        /**
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
    	ISIS_CIRC_IFINDEX,
    	
    	/**
    	 *  isisCircAdminState OBJECT-TYPE
         *      SYNTAX IsisAdminState
         *      MAX-ACCESS read-create
         *      STATUS current
         *      DESCRIPTION
         *         "The administrative state of the circuit."
         *      DEFVAL { off }
         *  ::= { isisCircEntry 3 }
    	 */
    	ISIS_CIRC_ADMIN_STATE

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
	        return getValue(ISIS_CIRC_IFINDEX).toInt();
	    }
	    
	    public Integer getIsisCircAdminState() {
	        return getValue(ISIS_CIRC_ADMIN_STATE).toInt();
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

    public IsisCircTableTracker(final RowCallback rowProcessor) {
        super(rowProcessor,isisCirctable_elemList);
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
    }


}
