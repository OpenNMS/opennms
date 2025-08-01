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
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("id", this.getId())
                .add("reductionKeys", this.getReductionKeys())
                .add("status", this.getOperationalStatus())
                .add("mapFunction", this.getMapFunction())
                .add("source", this.getSource() == null ? getSource() : getSource().getId())
                .toString();
    }
}
