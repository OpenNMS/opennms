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
package org.opennms.features.topology.api.support.breadcrumbs;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.support.VertexRefAdapter;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.VertexRef;

import com.google.common.collect.Lists;

/**
 * Element to describe a breadcrumb.
 *
 * @author mvrueden
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="breadcrumb")
public class Breadcrumb implements ClickListener {

    @XmlElement(name="source-vertex")
    @XmlElementWrapper(name="source-vertices")
    @XmlJavaTypeAdapter(value= VertexRefAdapter.class)
    private List<VertexRef> sourceVertices = Lists.newArrayList();

    @XmlElement(name="target-namespace")
    private String targetNamespace;

    // JAXB-Constructor
    protected Breadcrumb() {

    }

    public Breadcrumb(String namespace, VertexRef vertex) {
        this.targetNamespace = namespace;
        this.sourceVertices.add(vertex);
    }

    public Breadcrumb(String targetNamespace, List<VertexRef> vertices) {
        this.targetNamespace = targetNamespace;
        this.sourceVertices = Lists.newArrayList(vertices);
    }

    public Breadcrumb(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof Breadcrumb) {
            Breadcrumb other = (Breadcrumb) obj;
            boolean equals = Objects.equals(targetNamespace, other.targetNamespace)
                    && Objects.equals(sourceVertices, other.sourceVertices);
            return equals;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetNamespace, sourceVertices);
    }

    @Override
    public void clicked(GraphContainer graphContainer) {
        BreadcrumbCriteria criteria = Criteria.getSingleCriteriaForGraphContainer(graphContainer, BreadcrumbCriteria.class, false);
        if (criteria != null) {
            criteria.handleClick(this, graphContainer);
        }
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public List<VertexRef> getSourceVertices() {
        return sourceVertices.stream().sorted(Comparator.comparing(VertexRef::getLabel)).collect(Collectors.toList());
    }
}
