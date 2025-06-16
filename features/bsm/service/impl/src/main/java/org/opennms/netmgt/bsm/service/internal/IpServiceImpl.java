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
package org.opennms.netmgt.bsm.service.internal;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import org.opennms.netmgt.dao.util.ReductionKeyHelper;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.model.OnmsMonitoredService;

public class IpServiceImpl implements IpService {

    private final BusinessServiceManager m_manager;

    private final OnmsMonitoredService m_entity;

    public IpServiceImpl(final BusinessServiceManager manager,
                         final OnmsMonitoredService entity) {
        this.m_manager = manager;
        this.m_entity = entity;
    }

    public OnmsMonitoredService getEntity() {
        return m_entity;
    }

    @Override
    public int getId() {
        return m_entity.getId();
    }

    @Override
    public String getServiceName() {
        return m_entity.getServiceName();
    }

    @Override
    public String getNodeLabel() {
        if (m_entity.getNodeId() != null) {
            return m_manager.getNodeById(m_entity.getNodeId()).getLabel();
        }
        return null;
    }

    @Override
    public Integer getNodeId() {
        if (m_entity.getNodeId() != null) {
            return m_manager.getNodeById(m_entity.getNodeId()).getId();
        }
        return null;
    }

    @Override
    public String getIpAddress() {
        return m_entity.getIpAddress().toString();
    }

    @Override
    public Set<String> getReductionKeys() {
        return Collections.unmodifiableSet(ReductionKeyHelper.getReductionKeys(m_entity));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (getClass() != obj.getClass()) return false;
        final IpServiceImpl other = (IpServiceImpl) obj;
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
                .add("serviceName", this.getServiceName())
                .add("nodeLabel", this.getNodeLabel())
                .add("ipAddress", this.getIpAddress())
                .add("reductionKeys", this.getReductionKeys())
                .toString();
    }
}
