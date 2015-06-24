/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.linkd.snmp;

import org.opennms.netmgt.snmp.NamedSnmpVar;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpStore;

public class IsisCircTableEntry extends SnmpStore {

    public final static String ISIS_CIRC_IF_ALIAS    = "isisCircIfIndex";
    public final static String ISIS_CIRC_IF_OID       = ".1.3.6.1.2.1.138.1.3.2.1.2";

    private boolean hasIndex = false;
    private Integer m_isisCircIndex;

    public static final NamedSnmpVar[] isisCirctable_elemList = new NamedSnmpVar[] {
        
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
        new NamedSnmpVar(NamedSnmpVar.SNMPINT32, ISIS_CIRC_IF_ALIAS, ISIS_CIRC_IF_OID, 1)
    };
    
    public static final String TABLE_OID = ".1.3.6.1.2.1.138.1.3.2"; // start of table (GETNEXT)
    
    public IsisCircTableEntry() {
        super(isisCirctable_elemList);
    }

    
    public Integer getIsisCircIfIndex() {
        return getInt32(ISIS_CIRC_IF_ALIAS);
    }
    
    public Integer getIsisCircIndex() {
        return m_isisCircIndex;
    }
        
    /** {@inheritDoc} */
    @Override
    public void storeResult(SnmpResult res) {
            if (!hasIndex) {
                    m_isisCircIndex  = res.getInstance().getLastSubId();
                    hasIndex = true;
            }
            super.storeResult(res);
    }

}
