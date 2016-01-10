/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.bsm;

import java.util.Objects;

import org.opennms.features.topology.api.NamespaceAware;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.VertexRef;

public class BusinessServiceCriteria extends Criteria implements NamespaceAware {

    private String businessServiceId;

    public BusinessServiceCriteria() {

    }

    protected BusinessServiceCriteria(String businessServiceId) {
        this.businessServiceId = businessServiceId;
    }

    @Override
    public ElementType getType() {
        return ElementType.VERTEX;
    }

    @Override
    public String getNamespace() {
        return BusinessServicesTopologyProvider.TOPOLOGY_NAMESPACE;
    }

    public boolean apply(VertexRef vertexRef) {
        if (contributesTo(vertexRef.getNamespace())) {
            final BusinessServiceVertex businessServiceVertex = (BusinessServiceVertex) vertexRef;
            return businessServiceVertex.isPartOf(businessServiceId);
        }
        return false;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return getNamespace() != null && Objects.equals(namespace, getNamespace());
    }

    @Override
    public int hashCode() {
        return Objects.hash(businessServiceId);
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
        BusinessServiceCriteria other = (BusinessServiceCriteria) obj;
        return Objects.equals(businessServiceId, other.businessServiceId);
    }

    public void setBusinessServiceId(String businessServiceId) {
        this.businessServiceId = businessServiceId;
    }

    public String getBusinessServiceId() {
        return businessServiceId;
    }
}
