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
package org.opennms.netmgt.snmp.joesnmp;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpPduRequest;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpVarBind;

public class JoeSnmpV2TrapBuilder implements SnmpTrapBuilder {

    SnmpPduRequest m_pdu;
    
    public JoeSnmpV2TrapBuilder() {
        m_pdu = new SnmpPduRequest(SnmpPduPacket.V2TRAP);
        m_pdu.setRequestId(SnmpPduPacket.nextSequence());
    }
    
    @Override
    public void send(String destAddr, int destPort, String community) throws Exception {
        JoeSnmpStrategy.send(destAddr, destPort, community, m_pdu);
    }

    @Override
    public void sendTest(String destAddr, int destPort, String community) throws Exception {
        JoeSnmpStrategy.sendTest(destAddr, destPort, community, m_pdu);
    }
    
    @Override
    public void addVarBind(SnmpObjId name, SnmpValue value) {
        SnmpSyntax val = ((JoeSnmpValue) value).getSnmpSyntax();
        m_pdu.addVarBind(new SnmpVarBind(new SnmpObjectId(name.getIds()), val));
    }


}
