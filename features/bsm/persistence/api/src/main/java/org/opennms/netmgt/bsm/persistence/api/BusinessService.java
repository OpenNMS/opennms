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

    private Set<AbstractBusinessServiceEdge> m_edges = Sets.newLinkedHashSet();

    private AbstractReductionFunction m_reductionFunction;

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

    public void setAttribute(String key, String value) {
        m_attributes.put(key, value);
    }

    public String removeAttribute(String key) {
        return m_attributes.remove(key);
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy="businessService")
    public Set<AbstractBusinessServiceEdge> getEdges() {
        return m_edges;
    }

    public void setEdges(Set<AbstractBusinessServiceEdge> edges) {
        m_edges = edges;
    }

    public void addEdge(AbstractBusinessServiceEdge edge) {
        m_edges.add(edge);
    }

    public void removeEdge(AbstractBusinessServiceEdge edge) {
        m_edges.remove(edge);
    }

    @Transient
    @SuppressWarnings("unchecked")
    public <T extends AbstractBusinessServiceEdge> Set<T> getEdges(Class<T> type) {
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
                && com.google.common.base.Objects.equal(m_edges, other.m_edges);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(m_id, m_name, m_attributes, m_edges);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this).add("id", m_id).add("name", m_name)
                .add("attributes", m_attributes)
                .add("edges", m_edges)
                .toString();
    }
}
