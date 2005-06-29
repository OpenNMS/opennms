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
        adaptConfig(agentConfig);
        SnmpSession session = null;
        JoeSnmpValue value = null;
        
        try {
            SnmpPeer peer = createPeer(agentConfig);

            SnmpParameters params = new SnmpParameters();
            setParameters(agentConfig, params);
            peer.setParameters(params);
            
            configurePeer(peer, agentConfig);
            
            session = new SnmpSession(peer);
            SnmpSyntax result = session.get(new SnmpObjectId(oid.toString()));
            value = new JoeSnmpValue(result);
        } catch (SocketException e) {
            log().error("Could not create JoeSNMP session using AgentConfig: "+agentConfig);
        } finally {
            session.close();
        }
        return value;
    }

    private void configurePeer(SnmpPeer peer, SnmpAgentConfig agentConfig) {
        peer.setPort(agentConfig.getPort());
        peer.setRetries(agentConfig.getRetries());
        peer.setTimeout(agentConfig.getTimeout());
    }

    public SnmpValue getNext(SnmpAgentConfig agentConfig, SnmpObjId oid) {
        adaptConfig(agentConfig);
        SnmpSession session = null;
        JoeSnmpValue value = null;
        
        try {
            SnmpPeer peer = createPeer(agentConfig);

            SnmpParameters params = new SnmpParameters();
            setParameters(agentConfig, params);
            peer.setParameters(params);
            
            configurePeer(peer, agentConfig);
            
            session = new SnmpSession(peer);
            SnmpSyntax result = session.get(new SnmpObjectId(oid.toString()));
            value = new JoeSnmpValue(result);
        } catch (SocketException e) {
            log().error("Could not create JoeSNMP session using AgentConfig: "+agentConfig);
        } finally {
            session.close();
        }
        return value;
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
