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

import java.util.Objects;

public class OnmsTopologyPort extends OnmsTopologyAbstractRef implements OnmsTopologyRef {

    public static OnmsTopologyPort create(String id, OnmsTopologyVertex vertex, Integer index) {
        Objects.requireNonNull(id, "Cannot create port, id is null");
        Objects.requireNonNull(vertex, "Cannot create port, vertex is null");

        if (index != null) {
            return new OnmsTopologyPort(id, vertex, index);
        }
        return new OnmsTopologyPort(id, vertex, -1);
    }

    private final OnmsTopologyVertex m_vertex;
    private final Integer m_index;
    private Integer m_ifindex;
    private String m_ifname;
    
    private String m_addr;
    private String m_speed;
    

    private OnmsTopologyPort(String id, OnmsTopologyVertex vertex, Integer index) {
        super(id);
        m_vertex = vertex;
        m_index = index;
    }

    public String getAddr() {
        return m_addr;
    }


    public void setAddr(String addr) {
        m_addr = addr;
    }


    public String getSpeed() {
        return m_speed;
    }


    public void setSpeed(String speed) {
        m_speed = speed;
    }


    public OnmsTopologyVertex getVertex() {
        return m_vertex;
    }


    public Integer getIndex() {
        return m_index;
    }


    public Integer getIfindex() {
        return m_ifindex;
    }


    public void setIfindex(Integer ifindex) {
        m_ifindex = ifindex;
    }


    public String getIfname() {
        return m_ifname;
    }


    public void setIfname(String ifname) {
        m_ifname = ifname;
    }

    @Override
    public void accept(TopologyVisitor v) {
        v.visit(this);
    }

}