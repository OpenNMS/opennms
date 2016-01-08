/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.persistence.api;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.opennms.netmgt.model.OnmsMonitoredService;

@Entity
@Table(name = "bsm_service")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class BusinessService {

    private Long m_id;

    private String m_name;

    private Map<String, String> m_attributes = Maps.newLinkedHashMap();

    private Set<OnmsMonitoredService> m_ipServices = Sets.newLinkedHashSet();

    private Set<String> m_reductionKeys = Sets.newLinkedHashSet();

    private Set<BusinessService> m_childServices = Sets.newLinkedHashSet();

    private Set<BusinessService> m_parentServices = Sets.newLinkedHashSet();

    @Id
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    @Column(name = "id", nullable = false)
    public Long getId() {
        return m_id;
    }

    public void setId(Long id) {
        m_id = id;
    }

    @Column(name = "name", nullable = false)
    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    @ElementCollection
    @JoinTable(name = "bsm_service_attributes", joinColumns = @JoinColumn(name = "bsm_service_id", referencedColumnName = "id"))
    @MapKeyColumn(name = "key")
    @Column(name = "value", nullable = false)
    public Map<String, String> getAttributes() {
        return m_attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        m_attributes = attributes;
    }

    public void setAttribute(String key, String value) {
        m_attributes.put(key, value);
    }

    public String removeAttribute(String key) {
        return m_attributes.remove(key);
    }

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "bsm_service_ifservices",
               joinColumns = @JoinColumn(name = "bsm_service_id", referencedColumnName = "id"),
               inverseJoinColumns = @JoinColumn(name="ifserviceid"))
    public Set<OnmsMonitoredService> getIpServices() {
        return m_ipServices;
    }

    public void setIpServices(Set<OnmsMonitoredService> ipServices) {
        m_ipServices = ipServices;
    }

    public void addIpService(OnmsMonitoredService ipService) {
        m_ipServices.add(ipService);
    }

    public void removeIpService(OnmsMonitoredService ipService) {
        m_ipServices.remove(ipService);
    }

    @Transient
    private Set<Integer> getIpServiceIds() {
        return m_ipServices.stream()
            .map(ipSvc -> ipSvc.getId())
            .collect(Collectors.toSet());
    }

    @ElementCollection
    @JoinTable(name = "bsm_service_reductionkeys", joinColumns = @JoinColumn(name = "bsm_service_id", referencedColumnName = "id"))
    @Column(name = "reductionkey", nullable = false)
    public Set<String> getReductionKeys() {
        return m_reductionKeys;
    }

    public void setReductionKeys(Set<String> m_reductionKeys) {
        this.m_reductionKeys = m_reductionKeys;
    }

    public void addReductionKey(String reductionKey) {
        m_reductionKeys.add(reductionKey);
    }

    public void addReductionKeys(Set<String> reductionKeys) {
        m_reductionKeys.addAll(reductionKeys);
    }

    @Transient
    public Set<String> getAllReductionKeys() {
        Set<String> allReductionKeys = Sets.newHashSet();
        for (OnmsMonitoredService ipService : getIpServices()) {
            allReductionKeys.addAll(OnmsMonitoredServiceHelper.getReductionKeys(ipService));
        }
        allReductionKeys.addAll(getReductionKeys());
        return allReductionKeys;
    }

    @ManyToMany(fetch = FetchType.EAGER,
                cascade = CascadeType.ALL)
    @JoinTable(name = "bsm_service_children",
               joinColumns = @JoinColumn(name = "bsm_service_parent", referencedColumnName = "id"),
               inverseJoinColumns = @JoinColumn(name="bsm_service_child", referencedColumnName = "id"))
    public Set<BusinessService> getChildServices() {
        return m_childServices;
    }

    public void setChildServices(Set<BusinessService> childServices) {
        m_childServices = childServices;
    }

    public void addChildService(BusinessService childService) {
        m_childServices.add(childService);
    }

    public void removeChildService(BusinessService childService) {
        m_childServices.remove(childService);
    }

    @ManyToMany(fetch = FetchType.EAGER,
                cascade = CascadeType.ALL,
                mappedBy = "childServices")
    public Set<BusinessService> getParentServices() {
        return m_parentServices;
    }

    public void setParentServices(Set<BusinessService> parentServices) {
        m_parentServices = parentServices;
    }

    public void addParentService(BusinessService parentService) {
        m_parentServices.add(parentService);
    }

    public void removeParentService(BusinessService parentService) {
        m_parentServices.remove(parentService);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BusinessService other = (BusinessService) obj;

        return com.google.common.base.Objects.equal(m_id, other.m_id)
                && com.google.common.base.Objects.equal(m_name, other.m_name)
                && com.google.common.base.Objects.equal(m_attributes, other.m_attributes)
                // OnmsMonitoredService objects don't properly support the equals() and hashCode() methods
                // so we resort to comparing their IDs, which is sufficient in the case of the Business Service
                && com.google.common.base.Objects.equal(getIpServiceIds(), other.getIpServiceIds());
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(m_id, m_name, m_attributes, getIpServiceIds());
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this).add("id", m_id).add("name", m_name)
                .add("attributes", m_attributes)
                .add("ipServices", m_ipServices)
                .toString();
    }
}
