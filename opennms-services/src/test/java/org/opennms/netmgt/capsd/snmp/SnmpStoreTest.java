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
