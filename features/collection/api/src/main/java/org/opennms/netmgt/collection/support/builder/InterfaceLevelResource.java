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
package org.opennms.netmgt.collection.support.builder;

import java.util.Objects;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.netmgt.collection.adapters.InterfaceLevelResourceAdapter;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.model.ResourcePath;

@XmlJavaTypeAdapter(InterfaceLevelResourceAdapter.class)
public class InterfaceLevelResource extends AbstractResource {

    private NodeLevelResource m_node;
    private String m_ifName;

    public InterfaceLevelResource(NodeLevelResource node, String ifName) {
        m_node = node;
        m_ifName = ifName;
    }

    public String getIfName() {
        return m_ifName;
    }

    @Override
    public NodeLevelResource getParent() {
        return m_node;
    }

    @Override
    public String getInstance() {
        return m_ifName;
    }

    @Override
    public String getUnmodifiedInstance() {
        return getInstance();
    }

    @Override
    public String getLabel(CollectionResource resource) {
        return m_ifName;
    }

    @Override
    public ResourcePath getPath(CollectionResource resource) {
        return ResourcePath.get(getIfName());
    }

    @Override
    public String getTypeName() {
        return CollectionResource.RESOURCE_TYPE_IF;
    }

    @Override
    public String toString() {
        return String.format("InterfaceLevelResource[node=%s, ifName=%s]", m_node, m_ifName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_node, m_ifName, getTimestamp());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof InterfaceLevelResource)) {
            return false;
        }
        InterfaceLevelResource other = (InterfaceLevelResource) obj;
        return Objects.equals(this.m_node, other.m_node)
                && Objects.equals(this.m_ifName, other.m_ifName)
                && Objects.equals(this.getTimestamp(), other.getTimestamp());
    }
}
