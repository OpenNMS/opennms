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
import java.util.Objects;
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
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.opennms.netmgt.bsm.mapreduce.api.Edge;
import org.opennms.netmgt.model.OnmsMonitoredService;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Entity
@Table(name = "bsm_service")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // TODO MVR do we need this?
public class BusinessServiceEntity {

    private Long m_id;

    private String m_name;

    private Map<String, String> m_attributes = Maps.newLinkedHashMap();

    private Set<BusinessServiceEdge> m_edges = Sets.newLinkedHashSet();

    private AbstractReductionFunction m_reductionFunction;

    /** The level in the hierarchy.
     * If 0 the business service should not have any parents. */
    private Integer level;

    public void setLevel(int level) {
        this.level = level;
    }

    @Transient
    public Integer getLevel() {
        return level;
    }

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

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "bsm_service_attributes", joinColumns = @JoinColumn(name = "bsm_service_id", referencedColumnName = "id"))
    @MapKeyColumn(name = "key")
    @Column(name = "value", nullable = false)
    public Map<String, String> getAttributes() {
        return m_attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        m_attributes = attributes;
    }

    @OneToMany(fetch = FetchType.EAGER,
               cascade = CascadeType.ALL)
    // TODO MVR we have an annotation at the setter. Why and how does this affect the annotations on the getter?
    public void setAttribute(String key, String value) {
        m_attributes.put(key, value);
    }

    // TODO MVR verify the cascade type
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy="businessService")
    public Set<BusinessServiceEdge> getEdges() {
        return m_edges;
    }

    public void setEdges(Set<BusinessServiceEdge> edges) {
        m_edges = edges;
    }

    public void addEdge(BusinessServiceEdge edge) {
        m_edges.add(edge);
    }

    @Transient
    public Set<IPServiceEdge> getIpServiceEdges() {
        return getEdges(IPServiceEdge.class);
    }

    @Transient
    @SuppressWarnings("unchecked")
    public <T extends BusinessServiceEdge> Set<T> getEdges(Class<T> type) {
        return getEdges().stream()
                .filter(e -> type.isInstance(e))
                .map(e -> (T)e)
                .collect(Collectors.toSet());
    }

    @ManyToOne
    @JoinColumn(name = "bsm_reduce_id")
    public AbstractReductionFunction getReductionFunction() {
        return m_reductionFunction;
    }

    public void setReductionFunction(AbstractReductionFunction reductionFunction) {
        m_reductionFunction = Objects.requireNonNull(reductionFunction);
    }

    // TODO MVR we do not need this anymore as getEdges().getReductionKeys() is basically the same
    @Transient
    public Set<String> getAllReductionKeys() {
        Set<String> allReductionKeys = Sets.newHashSet();
        for (Edge eachEdge : getEdges()) {
            allReductionKeys.addAll(eachEdge.getReductionKeys());
        }
        return allReductionKeys;
    }

    // TODO MVR MERGE ME (e.g. rename to getChildEdges())
//    @ManyToMany(fetch = FetchType.EAGER,
//                cascade = CascadeType.ALL)
//    @JoinTable(name = "bsm_service_children",
//               joinColumns = @JoinColumn(name = "bsm_service_parent", referencedColumnName = "id"),
//               inverseJoinColumns = @JoinColumn(name="bsm_service_child", referencedColumnName = "id"))
    // Convenient method to retrieve all Business Services children
    @Transient
    public Set<BusinessServiceEntity> getChildServices() {
        Set<BusinessServiceEntity> childServices = Sets.newHashSet();
        for (BusinessServiceChildEdge eachEdge : getEdges(BusinessServiceChildEdge.class)) {
            childServices.add(eachEdge.getChild());
        }
        return childServices;
    }

    // Convenient method to retrieve all Ip Services
    @Transient
    public Set<OnmsMonitoredService> getIpServices() {
        Set<OnmsMonitoredService> monitoredServices = Sets.newHashSet();
        for (IPServiceEdge eachEdge : getEdges(IPServiceEdge.class)) {
            monitoredServices.add(eachEdge.getIpService());
        }
        return monitoredServices;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BusinessServiceEntity other = (BusinessServiceEntity) obj;
        return com.google.common.base.Objects.equal(m_id, other.m_id)
                && com.google.common.base.Objects.equal(m_name, other.m_name)
                && com.google.common.base.Objects.equal(m_attributes, other.m_attributes)
                && com.google.common.base.Objects.equal(m_edges, other.m_edges);
//        final BusinessServiceEntity other = (BusinessServiceEntity) obj;
//        return Objects.equals(getId(), other.getId())
//                && Objects.equals(getName(), other.getName());
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(m_id, m_name, m_attributes, m_edges);
//        return Objects.hash(m_id, m_name);
    }

    @Override
    public String toString() {
        // we do not include ip services here, otherwise we cannot use this object properly
        return com.google.common.base.Objects.toStringHelper(this)
                .add("id", m_id)
                .add("name", m_name)
                .add("attributes", m_attributes)
                .add("edges", m_edges)
//                .add("childServices", m_childServices)
                .toString();
    }

    public BusinessServiceEntity addChildren(BusinessServiceEntity children, AbstractMapFunction mapFunction) {
        if (!getChildServices().contains(Objects.requireNonNull(children))) {
            BusinessServiceChildEdge edge = new BusinessServiceChildEdge();
            edge.setBusinessService(this);
            edge.setChild(children);
            edge.setMapFunction(Objects.requireNonNull(mapFunction));
            addEdge(edge);
        }
        return this;
    }

    // Convinent method to add an ipservice
    public BusinessServiceEntity addIpService(OnmsMonitoredService ipService, AbstractMapFunction mapFunction) {
        if (!getIpServices().contains(Objects.requireNonNull(ipService))) {
            IPServiceEdge edge = new IPServiceEdge();
            edge.setBusinessService(this);
            edge.setIpService(ipService);
            edge.setMapFunction(Objects.requireNonNull(mapFunction));
            addEdge(edge);
        }
        return this;
    }

    public void addReductionKey(String reductionKey, AbstractMapFunction mapFunction) {
        // TODO MVR handle that not already existing reduction key can be added
        SingleReductionKeyEdge edge = new SingleReductionKeyEdge();
        edge.setBusinessService(this);
        edge.setReductionKey(reductionKey);
        edge.setMapFunction(Objects.requireNonNull(mapFunction));
        addEdge(edge);
    }
}
