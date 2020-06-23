/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.capsd.snmp;

import junit.framework.TestCase;

import org.opennms.netmgt.snmp.NamedSnmpVar;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpStore;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;

/**
 * We are trying to make sure that this doesn't happen:
 * <pre>
 * 2008-05-10 14:32:38,044 DEBUG [DefaultUDPTransportMapping_172.16.1.194/0] org.opennms.netmgt.capsd.snmp.IfXTableEntry: Storing Result: alias: ifAlias [.1.3.6.1.2.1.31.1.1.1.18].[1] = (4) We don't need no stinkin' ifAlias! (576520646f6e2774206e656564206e6f207374696e6b696e27206966416c69617321)
 * 2008-05-10 14:32:38,044 DEBUG [DefaultUDPTransportMapping_172.16.1.194/0] org.opennms.netmgt.capsd.snmp.IfXTableEntry: Storing Result: alias: ifAlias [.1.3.6.1.2.1.31.1.1.1.18].[1] = (130) endOfMibView
 * 2008-05-10 14:32:38,044 DEBUG [main] org.opennms.netmgt.capsd.IfSnmpCollector: getIfAlias: ifIndex 1 has ifAlias 'endOfMibView'
 * </pre>
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
  */
public class SnmpStoreTest extends TestCase {
    public void testStoreResultWithValueThenEndOfMibView() {
        String baseOid = ".1.3.6.1.2.1.31.1.1.1.18";
        String ifAliasName = "ifAlias";
        String ifAliasValue = "Foo!";
        
        SnmpStore store = new SnmpStore(new NamedSnmpVar[] { new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, ifAliasName, baseOid, 18) });
        SnmpObjId base = SnmpObjId.get(baseOid);
        SnmpInstId inst = new SnmpInstId("1");
        
        store.storeResult(new SnmpResult(base, inst, SnmpUtils.getValueFactory().getOctetString(ifAliasValue.getBytes())));
        store.storeResult(new SnmpResult(base, inst, SnmpUtils.getValueFactory().getValue(SnmpValue.SNMP_END_OF_MIB, null)));
        
        assertEquals("ifAlias value", ifAliasValue, store.getDisplayString(ifAliasName));
    }
}
