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
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpStore;
import org.opennms.netmgt.snmp.SnmpUtils;

public class MtxrWlRtabTableEntry extends SnmpStore {

    public final static String MTRX_WL_RTAB_ADDR= "mtxrWlRtabAddr";
    public final static String MTRX_WL_RTAB_IFACE= "mtxrWlRtabIface";
    
    public final static String MTRX_WL_RTAB_ADDR_OID  = ".1.3.6.1.4.1.14988.1.1.1.2.1.1";
    public final static String MTRX_WL_RTAB_IFACE_OID = ".1.3.6.1.4.1.14988.1.1.1.2.1.2";

    public static final NamedSnmpVar[] mtxrWlRtabtable_elemList = new NamedSnmpVar[] {
        
        new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, MTRX_WL_RTAB_ADDR, MTRX_WL_RTAB_ADDR_OID, 1),
        new NamedSnmpVar(NamedSnmpVar.SNMPINT32, MTRX_WL_RTAB_IFACE, MTRX_WL_RTAB_IFACE_OID, 2)
    };
    
    public static final String TABLE_OID = ".1.3.6.1.4.1.14988.1.1.1.2"; // start of table (GETNEXT)
    
    public MtxrWlRtabTableEntry() {
        super(mtxrWlRtabtable_elemList);
    }

    public String getMtxrWlRtabAddr() {
        return getHexString(MTRX_WL_RTAB_ADDR);
    }
    public Integer getMtxrWlRtabIface() {
        return getInt32(MTRX_WL_RTAB_IFACE);
    }
    
    /** {@inheritDoc} */
    @Override
    public void storeResult(SnmpResult res) {
            int iface = res.getInstance().getLastSubId();
            super.storeResult(res);
            super.storeResult(new SnmpResult(SnmpObjId.get(MTRX_WL_RTAB_IFACE_OID), res.getInstance(), 
                        SnmpUtils.getValueFactory().getInt32(iface)));
    }


}
