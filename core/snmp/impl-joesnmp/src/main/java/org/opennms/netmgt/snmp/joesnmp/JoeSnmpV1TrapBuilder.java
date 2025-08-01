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

import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpV1TrapBuilder;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.protocols.snmp.SnmpIPAddress;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpPduTrap;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpVarBind;

public class JoeSnmpV1TrapBuilder implements SnmpV1TrapBuilder {
    
    SnmpPduTrap trap = new SnmpPduTrap();

    @Override
    public void setEnterprise(SnmpObjId enterpriseId) {
        trap.setEnterprise(new SnmpObjectId(enterpriseId.getIds()));
    }

    @Override
    public void setAgentAddress(InetAddress agentAddress) {
        trap.setAgentAddress(new SnmpIPAddress(agentAddress));
    }

    @Override
    public void setGeneric(int generic) {
        trap.setGeneric(generic);
    }

    @Override
    public void setSpecific(int specific) {
        trap.setSpecific(specific);
    }

    @Override
    public void setTimeStamp(long timeStamp) {
        trap.setTimeStamp(timeStamp);
    }

    @Override
    public void send(String destAddr, int destPort, String community) throws Exception {
        JoeSnmpStrategy.send(destAddr, destPort, community, trap);
    }

    @Override
    public void sendTest(String destAddr, int destPort, String community) throws Exception {
        JoeSnmpStrategy.sendTest(destAddr, destPort, community, trap);
    }

    @Override
    public void addVarBind(SnmpObjId name, SnmpValue value) {
        SnmpSyntax val = ((JoeSnmpValue) value).getSnmpSyntax();
        trap.addVarBind(new SnmpVarBind(new SnmpObjectId(name.getIds()), val));
    }

}
