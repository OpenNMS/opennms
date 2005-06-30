//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.snmp.joesnmp;

import java.net.SocketException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpStrategy;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.netmgt.snmp.joesnmp.JoeSnmpWalker.JoeSnmpValue;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpParameters;
import org.opennms.protocols.snmp.SnmpPeer;
import org.opennms.protocols.snmp.SnmpSMI;
import org.opennms.protocols.snmp.SnmpSession;
import org.opennms.protocols.snmp.SnmpSyntax;

public class JoeSnmpStrategy implements SnmpStrategy {

    public SnmpWalker createWalker(SnmpAgentConfig agentConfig, String name, CollectionTracker tracker) {
        return new JoeSnmpWalker(agentConfig, name, tracker);
    }
    
    public SnmpValue get(SnmpAgentConfig agentConfig, SnmpObjId oid) {
        SnmpObjId[] oids = { oid };
        return get(agentConfig, oids)[0];
    }

    public SnmpValue[] get(SnmpAgentConfig agentConfig, SnmpObjId[] oids) {
        adaptConfig(agentConfig);
        SnmpSession session = null;
        SnmpValue[] values = { null };
        
        try {
            SnmpPeer peer = createPeer(agentConfig);

            SnmpParameters params = new SnmpParameters();
            setParameters(agentConfig, params);
            peer.setParameters(params);
            
            configurePeer(peer, agentConfig);
            
            session = new SnmpSession(peer);
            SnmpObjectId[] jOids = convertOids(oids);
            SnmpSyntax[] results = session.get(jOids);
            values = convertSnmpSyntaxs(results);
        } catch (SocketException e) {
            log().error("Could not create JoeSNMP session using AgentConfig: "+agentConfig);
        } finally {
            session.close();
        }
        return values;
    }
    
    public SnmpValue getNext(SnmpAgentConfig agentConfig, SnmpObjId oid) {
        SnmpObjId[] oids = { oid };
        return getNext(agentConfig, oids)[0];
    }

    public SnmpValue[] getNext(SnmpAgentConfig agentConfig, SnmpObjId[] oids) {
        adaptConfig(agentConfig);
        SnmpSession session = null;
        SnmpValue[] values = { null };
        
        try {
            SnmpPeer peer = createPeer(agentConfig);

            SnmpParameters params = new SnmpParameters();
            setParameters(agentConfig, params);
            peer.setParameters(params);
            
            configurePeer(peer, agentConfig);
            
            session = new SnmpSession(peer);
            SnmpObjectId[] jOids = convertOids(oids);
            
            SnmpSyntax[] results = session.getNext(jOids);
            values = convertSnmpSyntaxs(results);
        } catch (SocketException e) {
            log().error("Could not create JoeSNMP session using AgentConfig: "+agentConfig);
        } finally {
            session.close();
        }
        return values;
    }

    /**
     * Convert JoeSnmp SnmpSyntax array to OpenNMS SnmpValue array
     * 
     * @param results
     * @return
     *        values as an OpenNMS SnmpValue array
     */
    private SnmpValue[] convertSnmpSyntaxs(SnmpSyntax[] results) {
        
        SnmpValue[] values = { null };
        
        if (results == null || results[0] == null || results.length == 0)
            return values;
            
        values = new JoeSnmpValue[results.length];
        for (int i=0; i<results.length; i++) {
            values[i] = new JoeSnmpValue(results[i]);
        }
        return values;
    }

    /**
     * Convert the OpenNMS Generic SnmpObjId[] array to a JoeSnmp SnmpObjectId[]
     * 
     * @param oids
     * @return
     *        An array of JoeSnmp SnmpObjectIds
     */
    private SnmpObjectId[] convertOids(SnmpObjId[] oids) {
        
        SnmpObjectId[] jOids = new SnmpObjectId[oids.length];
        for (int i=0; i<oids.length; i++) {
            jOids[i] = new SnmpObjectId(oids[i].toString());
        }
        
        return jOids;
    }

    private void configurePeer(SnmpPeer peer, SnmpAgentConfig agentConfig) {
        peer.setPort(agentConfig.getPort());
        peer.setRetries(agentConfig.getRetries());
        peer.setTimeout(agentConfig.getTimeout());
    }

    private SnmpPeer createPeer(SnmpAgentConfig agentConfig) {
        return new SnmpPeer(agentConfig.getAddress());
    }

    private void setParameters(SnmpAgentConfig agentConfig, SnmpParameters params) {
        params.setVersion(agentConfig.getVersion());
        params.setReadCommunity(agentConfig.getReadCommunity());
        params.setWriteCommunity(agentConfig.getWriteCommunity());
    }

    private Category log() {
        return ThreadCategory.getInstance();
    }

    public SnmpValue[] getBulk(SnmpAgentConfig agentConfig, SnmpObjId[] oids) {
        // TODO Auto-generated method stub
        return null;
    }

    public static void adaptConfig(SnmpAgentConfig agentConfig) {
        agentConfig.setVersion(convertVersion(agentConfig.getVersion()));
    }

    public static int convertVersion(int version) {
        switch (version) {
        case SnmpAgentConfig.VERSION2C :
            return SnmpSMI.SNMPV2;
        default :
            return SnmpSMI.SNMPV1;
        }
    }

}
