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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.service.internal.edge;

import java.util.Objects;

import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEdgeEntity;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.internal.BusinessServiceImpl;
import org.opennms.netmgt.bsm.service.internal.MapFunctionMapper;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;

public abstract class AbstractEdge<T extends BusinessServiceEdgeEntity> implements Edge {

    private final T m_entity;

    private final BusinessServiceManager m_manager;

    public AbstractEdge(BusinessServiceManager manager, T entity) {
        m_manager = manager;
        m_entity = entity;
    }

    @Override
    public Long getId() {
        return m_entity.getId();
    }

    public T getEntity() {
        return m_entity;
    }

    @Override
    public BusinessService getSource() {
        return new BusinessServiceImpl(m_manager, m_entity.getBusinessService());
    }

    @Override
    public void setSource(BusinessService source) {
        getEntity().setBusinessService(((BusinessServiceImpl) source).getEntity());
    }

    @Override
    public Status getOperationalStatus() {
        return getManager().getOperationalStatus(this);
    }

    @Override
    public void setMapFunction(MapFunction mapFunction) {
        m_manager.setMapFunction(this, mapFunction);
    }

    @Override
    public MapFunction getMapFunction() {
        return new MapFunctionMapper().toServiceFunction(getEntity().getMapFunction());
    }

    @Override
    public int getWeight() {
        return getEntity().getWeight();
    }

    @Override
    public void setWeight(int weight) {
        getEntity().setWeight(weight);
    }

    @Override
    public void delete() {
        getManager().deleteEdge(this.getSource(), this);
    }

    protected BusinessServiceManager getManager() {
        return m_manager;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (getClass() != obj.getClass()) return false;
        final AbstractEdge<?> other = (AbstractEdge<?>) obj;
        return Objects.equals(getEntity(), other.getEntity());
    }

    @Override
    public int hashCode() {
        return getEntity().hashCode();
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("id", this.getId())
                .add("reductionKeys", this.getReductionKeys())
                .add("status", this.getOperationalStatus())
                .add("mapFunction", this.getMapFunction())
                .add("source", this.getSource() == null ? getSource() : getSource().getId())
                .toString();
    }
}
