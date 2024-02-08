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
package org.opennms.netmgt.snmp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnmpGetter extends TableTracker {

	/**
	 * The SnmpPeer object used to communicate via SNMP with the remote host.
	 */
    private final SnmpAgentConfig m_agentConfig;
    private final LocationAwareSnmpClient m_client;
    private final String m_location;
    private static final Logger LOG = LoggerFactory.getLogger(SnmpGetter.class);

    public SnmpGetter(SnmpAgentConfig peer, LocationAwareSnmpClient client, String location) {
        m_agentConfig = peer;
        m_client = client;
        m_location = location;
    }

   public SnmpValue get(SnmpObjId entryoid,Integer index) {
       SnmpObjId instance = SnmpObjId.get(new int[] {index});
       List<SnmpObjId> oids = new ArrayList<>(1);
           oids.add(SnmpObjId.get(entryoid, instance));
       List<SnmpValue> val = get(oids);
       if (val == null || val.size() != 1 || val.get(0) == null || val.get(0).isError()) 
           return null;
       return val.get(0);
   }   
   
   public List<SnmpValue> get(List<SnmpObjId> entryoids, Integer index) {
       SnmpObjId instance = SnmpObjId.get(new int[] {index});
       List<SnmpObjId> oids = new ArrayList<>(entryoids.size());
       for (SnmpObjId entryoid: entryoids)
           oids.add(SnmpObjId.get(entryoid, instance));
       return get(oids);
   }
   
   public List<SnmpValue> get(List<SnmpObjId> oids) {
       List<SnmpValue> val;
       LOG.debug("get: oids '{}'", oids);
       try {
           val = m_client.get(m_agentConfig, oids).withLocation(m_location).execute().get();
       } catch (InterruptedException e) {
           LOG.error("get: InterruptedException: snmp GET {}: {}",
                    oids, e.getMessage());
           return null;
       } catch (ExecutionException e) {
           LOG.error("get: ExecutionException: snmp GET {}: {}",
                    oids, e.getMessage());
           return null;
       }
       LOG.debug("get: oid '{}' found value '{}'", oids, val);
       if (val == null || val.size() != oids.size()) 
          return null;
       return val;
   }

}
