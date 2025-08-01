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
package org.opennms.netmgt.enlinkd.snmp;


import org.opennms.netmgt.enlinkd.model.CdpLink;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpGetter;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;

public class CdpInterfacePortNameGetter extends SnmpGetter {

    public final static SnmpObjId CDP_INTERFACE_NAME = SnmpObjId.get(".1.3.6.1.4.1.9.9.23.1.1.1.1.6");
    public final static SnmpObjId MIB2_INTERFACE_NAME = SnmpObjId.get(".1.3.6.1.2.1.2.2.1.2");
	/**
	 * The SnmpPeer object used to communicate via SNMP with the remote host.
	 */
    public CdpInterfacePortNameGetter(SnmpAgentConfig peer, LocationAwareSnmpClient client, String location) {
        super(peer, client,location);
    }

    public CdpLink get(CdpLink link) {
        SnmpValue ifName = getInterfaceNameFromCiscoCdpMib(link.getCdpCacheIfIndex());
        if (ifName == null) {
            ifName = getInterfaceNameFromMib2(link.getCdpCacheIfIndex());
        }
        if (ifName != null)
            link.setCdpInterfaceName(ifName.toDisplayString());
        return link;
    }
    
   public SnmpValue getInterfaceNameFromCiscoCdpMib(Integer ifindex) {
       return get(CDP_INTERFACE_NAME, ifindex);
   }
   
   public SnmpValue getInterfaceNameFromMib2(Integer ifindex) {
       return get(MIB2_INTERFACE_NAME,ifindex);
   }

   
}
