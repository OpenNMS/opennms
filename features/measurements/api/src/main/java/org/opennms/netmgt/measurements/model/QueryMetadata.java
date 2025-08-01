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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Sets;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="metadata")
public class QueryMetadata {
    @XmlElementWrapper(name="resources")
    @XmlElement(name="resource")
    private final List<QueryResource> resources;

    @XmlElementWrapper(name="nodes")
    @XmlElement(name="node")
    private final Set<QueryNode> nodes;

    public QueryMetadata() {
        this.resources = null;
        this.nodes = null;
    }

    public QueryMetadata(final List<QueryResource> resources) {
        this.resources = resources;
        if (resources != null) {
            final Set<QueryNode> nodes = Sets.newTreeSet();
            for (final QueryResource resource : resources) {
                final QueryNode node = resource.getNode();
                if (node != null) {
                    nodes.add(node);
                }
            }
            this.nodes = nodes;
        } else {
            this.nodes = null;
        }
    }

    public List<QueryResource> getResources() {
        return this.resources == null? new ArrayList<QueryResource>() : this.resources;
    }

    public Set<QueryNode> getNodes() {
        return this.nodes == null? new HashSet<QueryNode>() : this.nodes;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QueryMetadata other = (QueryMetadata) obj;

        return Objects.equals(this.resources, other.resources);
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.resources);
    }
    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("resources", this.resources)
                .add("nodes", this.nodes)
                .toString();
    }
}
