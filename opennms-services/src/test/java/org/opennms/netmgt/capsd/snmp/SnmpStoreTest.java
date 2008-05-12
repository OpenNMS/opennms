/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2008 May 10: Created this file. - dj@opennms.org
 *
 * Copyright (C) 2008 Daniel J. Gregor, Jr..  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.capsd.snmp;

import junit.framework.TestCase;

import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.mock.TestSnmpValue;

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
        
        store.storeResult(base, inst, new TestSnmpValue.StringSnmpValue(ifAliasValue));
        store.storeResult(base, inst, TestSnmpValue.END_OF_MIB);
        
        assertEquals("ifAlias value", ifAliasValue, store.getDisplayString(ifAliasName));
    }
}
