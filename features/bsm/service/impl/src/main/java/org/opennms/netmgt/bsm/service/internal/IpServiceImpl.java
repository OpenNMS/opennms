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

package org.opennms.netmgt.bsm.service.internal;

import com.google.common.base.Objects;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.model.OnmsMonitoredService;

public class IpServiceImpl implements IpService {

    private final BusinessServiceManagerImpl m_manager;

    private final OnmsMonitoredService m_entity;

    public IpServiceImpl(final BusinessServiceManagerImpl manager,
                         final OnmsMonitoredService entity) {
        this.m_manager = manager;
        this.m_entity = entity;
    }

    public OnmsMonitoredService getEntity() {
        return m_entity;
    }

    @Override public int getId() {
        return m_entity.getId();
    }

    @Override public String getServiceName() {
        return m_entity.getServiceName();
    }

    @Override public String getNodeLabel() {
        return m_manager.getNodeDao().get(m_entity.getNodeId()).getLabel();
    }

    @Override public String getIpAddress() {
        return m_entity.getIpAddress().toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final IpServiceImpl other = (IpServiceImpl) obj;

        return Objects.equal(this.getId(), other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getId());
    }

    @Override
    public String toString() {
        return getNodeLabel()+"/"+getIpAddress()+"/"+getServiceName();
    }
}
