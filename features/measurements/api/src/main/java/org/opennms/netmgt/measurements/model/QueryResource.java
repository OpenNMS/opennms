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
package org.opennms.netmgt.measurements.model;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="resource")
public class QueryResource {
    @XmlAttribute @XmlID private String id;
    @XmlAttribute(name="parent-id") private String parentId;
    @XmlAttribute private String label;
    @XmlAttribute private String name;

    /** implemented below in getNodeId() because Jackson JSON doesn't honor @XmlIDREF */
    private QueryNode node;

    public QueryResource() {
        this.id = null;
        this.parentId = null;
        this.label = null;
        this.name = null;
        this.node = null;
    }

    public QueryResource(final String id, final String parentId, final String label, final String name, final QueryNode node) {
        this.id = id;
        this.parentId = parentId;
        this.label = label;
        this.name = name;
        this.node = node;
    }

    public String getId() {
        return this.id;
    }

    public String getParentId() {
        return this.parentId;
    }

    public String getLabel() {
        return this.label;
    }

    public String getName() {
        return this.name;
    }

    public QueryNode getNode() {
        return this.node;
    }

    @XmlAttribute(name="node-id")
    public Integer getNodeId() {
        return this.node == null? null : this.node.getId();
    }

    public void setNodeId(final Integer id) {
        this.node = new QueryNode(id, null, null, null);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QueryResource other = (QueryResource) obj;

        return Objects.equals(this.id, other.id)
                && Objects.equals(this.parentId, other.parentId)
                && Objects.equals(this.label, other.label)
                && Objects.equals(this.name, other.name)
                && Objects.equals(this.node, other.node);
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.parentId, this.label, this.name, this.node);
    }
    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("id", this.id)
                .add("parentId", this.parentId)
                .add("label", this.label)
                .add("name", this.name)
                .add("node", this.node)
                .toString();
    }
}
