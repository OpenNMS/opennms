/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.snmp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TableTracker;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnmpGetter extends TableTracker {

	/**
	 * The SnmpPeer object used to communicate via SNMP with the remote host.
	 */
    private SnmpAgentConfig m_agentConfig;
    private Integer m_nodeid;
    private LocationAwareSnmpClient m_client;
    private String m_location;
    private static final Logger LOG = LoggerFactory.getLogger(SnmpGetter.class);

    public SnmpGetter(SnmpAgentConfig peer, LocationAwareSnmpClient client, String location, Integer nodeid) {
        m_agentConfig = peer;
        m_client = client;
        m_location = location;
        m_nodeid=nodeid;
    }

   public Integer getNodeId() {
       return m_nodeid;
   }
    
   public SnmpValue get(SnmpObjId entryoid,Integer index) {
       SnmpObjId instance = SnmpObjId.get(new int[] {index});
       List<SnmpObjId> oids = new ArrayList<SnmpObjId>(1);
           oids.add(SnmpObjId.get(entryoid, instance));
       List<SnmpValue> val= get(oids);
       if (val == null || val.size() != 1 || val.get(0) == null || val.get(0).isError()) 
           return null;
       return val.get(0);
   }   
   
   public List<SnmpValue> get(List<SnmpObjId> entryoids, Integer index) {
       SnmpObjId instance = SnmpObjId.get(new int[] {index});
       List<SnmpObjId> oids = new ArrayList<SnmpObjId>(entryoids.size());
       for (SnmpObjId entryoid: entryoids)
           oids.add(SnmpObjId.get(entryoid, instance));
       return get(oids);
   }
   
   public List<SnmpValue> get(List<SnmpObjId> oids) {
       List<SnmpValue> val= null;
       LOG.debug("get: oids '{}'", oids);
       try {
           val = m_client.get(m_agentConfig, oids).withLocation(m_location).execute().get();
       } catch (InterruptedException e) {
           LOG.error("run: node [{}]: InterruptedException: snmp GET {}: {}", 
                    getNodeId(), oids, e.getMessage());
           return null;
       } catch (ExecutionException e) {
           LOG.error("run: node [{}]: ExecutionException: snmp GET {}: {}", 
                    getNodeId(), oids,e.getMessage());
           return null;
       }
       LOG.debug("get: oid '{}' found value '{}'", oids, val);
       if (val == null || val.size() != oids.size()) 
          return null;
       return val;
   }

}
