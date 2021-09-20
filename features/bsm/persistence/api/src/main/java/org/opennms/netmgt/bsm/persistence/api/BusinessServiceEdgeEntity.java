/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.persistence.api;

import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.opennms.netmgt.bsm.persistence.api.functions.map.AbstractMapFunctionEntity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * Base edges that includes properties common to all edge types.
 *
 * Ideally this class would be abstract, but in some cases Hibernate may
 * try to instantiate this class.
 *
 * @author jwhite
 */
@Entity
@Table(name = "bsm_service_edge")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name="type", discriminatorType= DiscriminatorType.STRING)
@DiscriminatorValue(value="")
public class BusinessServiceEdgeEntity implements EdgeEntity {

    public static final int DEFAULT_WEIGHT = 1;

    private Long m_id;

    // The Business Service reference where this edge belongs to!
    private BusinessServiceEntity m_businessService;

    private boolean m_enabled = true;

    private int m_weight = DEFAULT_WEIGHT;

    private AbstractMapFunctionEntity m_mapFunction;

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

    @ManyToOne(optional=false)
    @JoinColumn(name="bsm_service_id")
    public BusinessServiceEntity getBusinessService() {
        return m_businessService;
    }

    public void setBusinessService(BusinessServiceEntity service) {
        m_businessService = Objects.requireNonNull(service);
    }

    @Column(name = "enabled", nullable = false)
    public boolean isEnabled() {
        return m_enabled;
    }

    public void setEnabled(boolean enabled) {
        m_enabled = enabled;
    }

    @Column(name = "weight", nullable = false)
    public int getWeight() {
        return m_weight;
    }

    @Override
    @Transient
    public Set<String> getReductionKeys() {
        return Sets.newHashSet();
    }

    public void setWeight(int weight) {
        Preconditions.checkArgument(weight > 0, "weight must be strictly positive.");
        m_weight = weight;
    }

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bsm_map_id")
    public AbstractMapFunctionEntity getMapFunction() {
        return m_mapFunction;
    }

    public void setMapFunction(AbstractMapFunctionEntity mapFunction) {
        m_mapFunction = Objects.requireNonNull(mapFunction);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof BusinessServiceEdgeEntity)) return false;
        final BusinessServiceEdgeEntity other = (BusinessServiceEdgeEntity) obj;
        if (getId() != null) {
            return getId().equals(other.getId());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return 0; // HACK: always return 0, as otherwise Sets etc do not work.
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("id", m_id)
                .add("businessService", m_businessService == null ? null : m_businessService.getId())
                .add("enabled", m_enabled)
                .add("weight", m_weight)
                .add("mapFunction", m_mapFunction)
                .toString();
    }

    /**
     * Defines if the definition of the edge is equal to the given one.
     * This is quite different than the equals method of the object itself.
     *
     * @return true if equal, otherwise false
     */
    public boolean equalsDefinition(BusinessServiceEdgeEntity other) {
        if (other == null) return false;
        if (!getClass().equals(other.getClass())) return false;
        boolean equals = Objects.equals(getWeight(), other.getWeight())
                && Objects.equals(getBusinessService().getId(), other.getBusinessService().getId())
                && getMapFunction().equalsDefinition(other.getMapFunction());
        return equals;
    }

    @Override
    public <T> T accept(EdgeEntityVisitor<T> visitor) {
        // ALl sub classes MUST overwrite this properly, as this class cannot be abstract.
        // This is due to how hibernate deals with inheritance strategies.
        throw new IllegalStateException("Class '" + getClass().getName() + "' did not overwrite accept(EdgeEntityVisitor) method properly");
    }
}
