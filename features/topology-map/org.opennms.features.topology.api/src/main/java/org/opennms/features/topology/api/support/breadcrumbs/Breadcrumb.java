/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
