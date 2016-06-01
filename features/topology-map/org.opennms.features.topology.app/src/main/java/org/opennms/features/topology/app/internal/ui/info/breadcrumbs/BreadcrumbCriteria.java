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

package org.opennms.features.topology.app.internal.ui.info.breadcrumbs;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.VertexRef;

import com.google.common.collect.Lists;

/**
 * Criteria to store breadcrumbs in order allow Navigation backwards.
 *
 * @author mvrueden
 */
public class BreadcrumbCriteria extends Criteria {

    public static class Breadcrumb {
        private final String targetNamespace;
        private final VertexRef sourceVertex;

        public Breadcrumb(String targetNamespace, VertexRef sourceVertex) {
            this.targetNamespace = Objects.requireNonNull(targetNamespace);
            this.sourceVertex = Objects.requireNonNull(sourceVertex);
        }

        public String getTargetNamespace() {
            return targetNamespace;
        }

        public VertexRef getSourceVertex() {
            return sourceVertex;
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
                        && Objects.equals(sourceVertex, other.sourceVertex);
                return equals;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(targetNamespace, sourceVertex);
        }
    }

    private List<Breadcrumb> breadcrumbs = Lists.newArrayList();

    public void setNewRoot(VertexRef sourceVertex, String targetNamespace) {
        final Breadcrumb newRoot = new Breadcrumb(targetNamespace, sourceVertex);
        if (breadcrumbs.contains(newRoot)) {
            int index = breadcrumbs.indexOf(newRoot);
            breadcrumbs = breadcrumbs.subList(0, index + 1);
        } else {
            breadcrumbs.add(newRoot);
        }
    }

    public List<Breadcrumb> getBreadcrumbs() {
        return Collections.unmodifiableList(breadcrumbs);
    }

    @Override
    public ElementType getType() {
        return ElementType.GRAPH;
    }

    @Override
    public String getNamespace() {
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(breadcrumbs);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof BreadcrumbCriteria) {
            BreadcrumbCriteria other = (BreadcrumbCriteria) obj;
            boolean equals = Objects.equals(breadcrumbs, other.breadcrumbs);
            return equals;
        }
        return false;
    }

}
