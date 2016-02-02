/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.application;

import java.util.Objects;

import org.opennms.features.topology.api.NamespaceAware;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.VertexRef;

public class ApplicationCriteria extends Criteria implements NamespaceAware {

    private String applicationId;

    public ApplicationCriteria() {
        
    }

    protected ApplicationCriteria(String applicationId) {
        this.applicationId = applicationId;
    }

    @Override
    public ElementType getType() {
        return ElementType.VERTEX;
    }

    @Override
    public String getNamespace() {
        return ApplicationTopologyProvider.TOPOLOGY_NAMESPACE;
    }

    public boolean apply(VertexRef vertexRef) {
        if (contributesTo(vertexRef.getNamespace())) {
            final ApplicationVertex applicationVertex = (ApplicationVertex) vertexRef;
            return applicationVertex.isPartOf(applicationId);
        }
        return false;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return getNamespace() != null && Objects.equals(namespace, getNamespace());
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ApplicationCriteria other = (ApplicationCriteria) obj;
        return Objects.equals(applicationId, other.applicationId);
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationId() {
        return applicationId;
    }
}
