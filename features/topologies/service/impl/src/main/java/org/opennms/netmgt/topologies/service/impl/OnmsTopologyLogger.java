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
package org.opennms.netmgt.topologies.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.topologies.service.api.OnmsTopologyConsumer;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyMessage;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyPort;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnmsTopologyLogger implements OnmsTopologyConsumer {
    
    private final static Logger LOG = LoggerFactory.getLogger(OnmsTopologyLogger.class);

    private final OnmsTopologyProtocol m_protocol;
    private List<OnmsTopologyMessage> m_queue = new ArrayList<>();

    public OnmsTopologyLogger(String protocol) {
        m_protocol = OnmsTopologyProtocol.create(protocol);
    }

    @Override
    public String getName() {
        return m_protocol.getId()+":Consumer:Logger";
    }

    @Override
    public Set<OnmsTopologyProtocol> getProtocols() {
        Set<OnmsTopologyProtocol> protocols = new HashSet<>();
        protocols.add(m_protocol);
        return protocols;
    }

    @Override
    public void consume(OnmsTopologyMessage message) {
        m_queue.add(message);
        StringBuffer txt = new StringBuffer();
        txt.append(getName());
        txt.append("-");
        txt.append(message.getMessagestatus());
        txt.append("-");
        txt.append(message.getMessagebody().getId());
        message.getMessagebody().accept(new TopologyVisitor(txt));
        LOG.debug(txt.toString());
    }

    public List<OnmsTopologyMessage> getQueue() {
        synchronized (m_queue) {
            return new ArrayList<>(m_queue);            
        }
    }

    public OnmsTopologyProtocol getProtocol() {
        return m_protocol;
    }
    
    private class TopologyVisitor implements org.opennms.netmgt.topologies.service.api.TopologyVisitor {
        private final StringBuffer txt;

        TopologyVisitor(StringBuffer txt) {
            this.txt = txt;
        }

        @Override
        public void visit(OnmsTopologyVertex vertex) {
            txt.append(":");
            txt.append(vertex.getLabel());
        }

        @Override
        public void visit(OnmsTopologyEdge edge) {
            txt.append(":");
            OnmsTopologyPort source = edge.getSource();
            txt.append(source.getVertex().getId());
            txt.append(":");
            txt.append(source.getIfname());
            txt.append("|");
            OnmsTopologyPort target = edge.getTarget();
            txt.append(target.getVertex().getId());
            txt.append(":");
            txt.append(target.getIfname());
        }
    }

}
