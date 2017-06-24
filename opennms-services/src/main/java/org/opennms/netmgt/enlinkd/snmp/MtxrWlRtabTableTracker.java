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

import org.opennms.netmgt.snmp.RowCallback;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.TableTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MtxrWlRtabTableTracker extends TableTracker {

	private final static Logger LOG = LoggerFactory.getLogger(MtxrWlRtabTableTracker.class);
	
    public static final SnmpObjId MTRX_WL_RTAB_TABLE_ENTRY  = SnmpObjId.get(".1.3.6.1.4.1.14988.1.1.1.2.1"); // start of table (GETNEXT)
    
    public final static SnmpObjId MTRX_WL_RTAB_ADDR         = SnmpObjId.get(".1.3.6.1.4.1.14988.1.1.1.2.1.1");

    public static final SnmpObjId[] s_mtrxWlRTabRow_elemList = new SnmpObjId[] {
        
        /**
         *  "Wireless CAPSMAN registration table. It is indexed by remote
         *  mac-address and local interface index"
        */
        MTRX_WL_RTAB_ADDR
        /**
         * "Remote Mac Address."
         * 
         */        
    };
    
    public static class MtrxWlRTabRow extends SnmpRowResult {

    	public MtrxWlRTabRow(int columnCount, SnmpInstId instance) {
			super(columnCount, instance);
            LOG.debug( "column count = {}, instance = {}", columnCount, instance);
		}
    	
    	public String getMtxrWlCMRtabAddr() {
	        return getValue(MTRX_WL_RTAB_ADDR).toHexString();
	    }

    	public Integer getMtxrWlCMRtabIface() {
	        return getInstance().getLastSubId();
	    }
	    

    }
    
    

    public MtxrWlRtabTableTracker() {
        super(s_mtrxWlRTabRow_elemList);
    }

    public MtxrWlRtabTableTracker(final RowCallback rowProcessor) {
    	super(rowProcessor,s_mtrxWlRTabRow_elemList);
    }

    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new MtrxWlRTabRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processMtrxWlRTabRow((MtrxWlRTabRow)row);
    }

    /**
     * <p>processMtrxWlRTabRow</p>
     *
     * @param row a {@link org.opennms.netmgt.enlinkd.snmp.MtxrWlRtabTableTracker.MtrxWlRTabRow} object.
     */
    public void processMtrxWlRTabRow(final MtrxWlRTabRow row) {
    }


}
