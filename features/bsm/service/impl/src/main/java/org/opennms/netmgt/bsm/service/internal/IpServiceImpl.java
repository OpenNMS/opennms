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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
        return com.google.common.base.Objects.toStringHelper(this)
                .add("id", this.getId())
                .add("serviceName", this.getServiceName())
                .add("nodeLabel", this.getNodeLabel())
                .add("ipAddress", this.getIpAddress())
                .add("reductionKeys", this.getReductionKeys())
                .toString();
    }
}
