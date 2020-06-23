/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
import java.util.stream.Collectors;

import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.Application;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.dao.util.ReductionKeyHelper;
import org.opennms.netmgt.model.OnmsApplication;

import com.google.common.base.MoreObjects;

public class ApplicationImpl implements Application {

    private final BusinessServiceManager m_manager;

    private final OnmsApplication m_entity;

    public ApplicationImpl(final BusinessServiceManager manager,
                           final OnmsApplication entity) {
        this.m_manager = manager;
        this.m_entity = entity;
    }

    @Override
    public String getApplicationName() {
        return m_entity.getName();
    }

    public OnmsApplication getEntity() {
        return m_entity;
    }

    @Override
    public int getId() {
        return m_entity.getId();
    }

    @Override
    public Set<String> getReductionKeys() {
        return Collections.unmodifiableSet(ReductionKeyHelper.getReductionKeys(m_entity));
    }

    @Override
    public Set<IpService> getIpServices() {
        return Collections.unmodifiableSet(m_entity.getMonitoredServices().stream().map(e -> new IpServiceImpl(m_manager, e)).collect(Collectors.toSet()));
    }

    @Override
    public Set<Integer> getNodeIds() {
        return Collections.unmodifiableSet(m_entity.getMonitoredServices().stream().map(s -> s.getNodeId()).collect(Collectors.toSet()));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (getClass() != obj.getClass()) return false;
        final ApplicationImpl other = (ApplicationImpl) obj;
        return Objects.equals(getEntity(), other.getEntity());
    }

    @Override
    public int hashCode() {
        return getEntity().hashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("m_manager", m_manager)
                .add("m_entity", m_entity)
                .toString();
    }
}
