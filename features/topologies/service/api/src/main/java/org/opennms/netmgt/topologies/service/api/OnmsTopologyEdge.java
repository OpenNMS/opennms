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
package org.opennms.netmgt.topologies.service.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OnmsTopologyEdge extends OnmsTopologyAbstractRef implements OnmsTopologyRef {

    public static OnmsTopologyEdge create(String id, OnmsTopologyPort source, OnmsTopologyPort target) {
        Objects.requireNonNull(source, "source port null, cannot create edge");
        Objects.requireNonNull(target, "target port null, cannot create edge");

        if (source.getId().equals(target.getId())) {
            throw new IllegalArgumentException("target equals source port, cannot create edge");
        }
        
        return new OnmsTopologyEdge(id, source, target);
    }
        
    private final OnmsTopologyPort m_source;
    private final OnmsTopologyPort m_target;

    private OnmsTopologyEdge(String id, OnmsTopologyPort source, OnmsTopologyPort target) {
        super(id);
        m_source = source;
        m_target = target;
    }

    public OnmsTopologyPort getSource() {
        return m_source;
    }

    public OnmsTopologyPort getTarget() {
        return m_target;
    }

    public OnmsTopologyPort getPort(String id) {
        return getPorts().stream().filter(p -> id.equals(p.getId())).findAny().orElse(null);
    }
     
    public boolean hasPort(String id) {
           return (getPort(id) != null);
    }
    
    public List<OnmsTopologyPort> getPorts() {
        List<OnmsTopologyPort>ports = new ArrayList<>();
        ports.add(m_source);
        ports.add(m_target);
        return ports;
    }

    @Override
    public void accept(TopologyVisitor v) {
        v.visit(this);
    }
}
