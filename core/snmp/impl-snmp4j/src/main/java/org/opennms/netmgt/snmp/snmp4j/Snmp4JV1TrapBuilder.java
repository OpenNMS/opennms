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
package org.opennms.netmgt.snmp.snmp4j;

import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpV1TrapBuilder;
import org.snmp4j.PDUv1;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;

public class Snmp4JV1TrapBuilder extends Snmp4JV2TrapBuilder implements SnmpV1TrapBuilder {
    
    protected Snmp4JV1TrapBuilder(Snmp4JStrategy strategy) {
        super(strategy, new PDUv1(), PDUv1.V1TRAP);
    }
    
    protected PDUv1 getPDUv1() {
        return (PDUv1)getPDU();
    }
    
    @Override
    public void setEnterprise(SnmpObjId enterpriseId) {
        getPDUv1().setEnterprise(new OID(enterpriseId.getIds()));
    }

    @Override
    public void setAgentAddress(InetAddress agentAddress) {
        getPDUv1().setAgentAddress(new IpAddress(agentAddress));
    }

    @Override
    public void setGeneric(int generic) {
        getPDUv1().setGenericTrap(generic);
    }

    @Override
    public void setSpecific(int specific) {
        getPDUv1().setSpecificTrap(specific);
    }

    @Override
    public void setTimeStamp(long timeStamp) {
        getPDUv1().setTimestamp(timeStamp);
    }


}
