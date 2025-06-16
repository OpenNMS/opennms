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

public class OnmsTopologyVertex extends OnmsTopologyAbstractRef implements OnmsTopologyRef {

    public static OnmsTopologyVertex create(String id, String label, String address, String iconKey) {
        Objects.requireNonNull(id, "id is null, cannot create vertex");
        return new OnmsTopologyVertex(id, label, address, iconKey);
    }
    
    private Integer m_nodeid;
    private final String m_label;
    private final String m_address;
    private final String m_iconKey;

    private OnmsTopologyVertex(String id, String label, String address,String iconKey) {
        super(id);
        m_label=label;
        m_address = address;
        m_iconKey=iconKey;
    }

    public String getLabel() {
        return m_label;
    }

    public String getIconKey() {
        return m_iconKey;
    }

    public Integer getNodeid() {
        return m_nodeid;
    }

    public void setNodeid(Integer nodeid) {
        m_nodeid = nodeid;
    }

    public String getAddress() {
        return m_address;
    }

    @Override
    public void accept(TopologyVisitor v) {
        v.visit(this);
    }
}
