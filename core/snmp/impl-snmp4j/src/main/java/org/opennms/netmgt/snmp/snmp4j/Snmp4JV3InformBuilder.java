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

import org.opennms.netmgt.snmp.SnmpConfiguration;
import org.opennms.netmgt.snmp.SnmpV3TrapBuilder;
import org.opennms.netmgt.snmp.SnmpValue;
import org.snmp4j.ScopedPDU;

public class Snmp4JV3InformBuilder extends Snmp4JV2TrapBuilder implements SnmpV3TrapBuilder {
    
    protected Snmp4JV3InformBuilder(Snmp4JStrategy strategy) {
        super(strategy, new ScopedPDU(), ScopedPDU.INFORM);
    }
    
    @Override
    public SnmpValue[] sendInform(String destAddr, int destPort, int timeout, int retry, String community) throws Exception {
    	return super.sendInform(destAddr, destPort, 1000, 3, SnmpConfiguration.NOAUTH_NOPRIV, community, SnmpConfiguration.DEFAULT_AUTH_PASS_PHRASE,
    			SnmpConfiguration.DEFAULT_AUTH_PROTOCOL, SnmpConfiguration.DEFAULT_PRIV_PASS_PHRASE, SnmpConfiguration.DEFAULT_PRIV_PROTOCOL);
    }  
}
