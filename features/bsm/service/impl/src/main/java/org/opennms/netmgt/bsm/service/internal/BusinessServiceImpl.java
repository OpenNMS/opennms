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
 * http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.service.internal;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.model.OnmsSeverity;


public class BusinessServiceImpl implements BusinessService {

    private final BusinessServiceManagerImpl m_manager;

    private final BusinessServiceEntity m_entity;

    public BusinessServiceImpl(final BusinessServiceManagerImpl manager,
                               final BusinessServiceEntity entity) {
        this.m_manager = manager;
        this.m_entity = entity;
    }

    public BusinessServiceEntity getEntity() {
        return m_entity;
    }

    @Override
    public Long getId() {
        return m_entity.getId();
    }

    @Override
    public String getName() {
        return m_entity.getName();
    }

    @Override
    public void setName(String name) {
        m_entity.setName(name);
    }

    @Override
    public Map<String, String> getAttributes() {
        return m_entity.getAttributes();
    }

    @Override
    public void setAttributes(Map<String, String> attributes) {
        m_entity.setAttributes(attributes);
    }

    @Override
    public void addAttribute(String key, String value) {
        this.getAttributes().put(key, value);
    }

    @Override
    public String removeAttribute(String key) {
        return this.getAttributes().remove(key);
    }

    @Override
    public Set<IpService> getIpServices() {
        return m_entity.getIpServices().stream()
                       .map(s -> new IpServiceImpl(m_manager, s))
                       .collect(Collectors.toSet());
    }

    @Override
    public void setIpServices(Set<IpService> ipServices) {
        m_manager.setIpServices(this, ipServices);
    }

    @Override
    public void addIpService(IpService ipService) {
        m_manager.assignIpService(this, ipService);
    }

    @Override
    public void removeIpService(IpService ipService) {
        m_manager.removeIpService(this, ipService);
    }

    @Override
    public Set<BusinessService> getChildServices() {
        return m_entity.getChildServices().stream()
                       .map(s -> new BusinessServiceImpl(m_manager, s))
                       .collect(Collectors.toSet());
    }

    @Override
    public void setChildServices(final Set<BusinessService> childServices) {
        m_manager.setChildServices(this, childServices);
    }

    @Override
    public void addChildService(BusinessService childService) {
        m_manager.assignChildService(this, childService);
    }

    @Override
    public void removeChildService(BusinessService childService) {
        m_manager.removeChildService(this, childService);
    }


    @Override
    public Set<BusinessService> getParentServices() {
        return m_entity.getParentServices().stream()
                       .map(s -> new BusinessServiceImpl(m_manager, s))
                       .collect(Collectors.toSet());
    }

    @Override
    public void save() {
        this.m_manager.saveBusinessService(this);
    }

    @Override
    public void delete() {
        this.m_manager.deleteBusinessService(this);
    }

    @Override
    public Set<String> getReductionKeys() {
        return m_entity.getReductionKeys();

    }

    @Override
    public void setReductionKeys(Set<String> reductionKeySet) {
        m_entity.setReductionKeys(reductionKeySet);
    }

    @Override
    public OnmsSeverity getOperationalStatus() {
        return m_manager.getOperationalStatusForBusinessService(this);
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

        final BusinessService other = (BusinessService) obj;

        if (getId() != null) {
            return getId().equals(other.getId());
        } else {
            return super.equals(other);
        }
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId().hashCode();
        } else {
            return super.hashCode();
        }
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("id", this.getId())
                .add("name", this.getName())
                .add("attributes", this.getAttributes())
                .add("ipServices", this.getIpServices())
                .add("childServices", this.getChildServices())
                .add("reductionKeys", this.getReductionKeys())
                .add("operationalStatus", this.getOperationalStatus())
                .toString();
    }
}
