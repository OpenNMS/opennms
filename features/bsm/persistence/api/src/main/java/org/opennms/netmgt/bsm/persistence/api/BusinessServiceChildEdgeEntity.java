/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.persistence.api;

import java.util.Objects;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.google.common.collect.Sets;

@Entity
@Table(name = "bsm_service_children")
@PrimaryKeyJoinColumn(name="id")
@DiscriminatorValue(value="children")
public class BusinessServiceChildEdgeEntity extends BusinessServiceEdgeEntity {

    // The Business Service Entity where the parent points to (child relationship)
    private BusinessServiceEntity child;

    public void setChild(BusinessServiceEntity child) {
        this.child = child;
    }

    @ManyToOne(optional=false)
    @JoinColumn(name="bsm_service_child_id", nullable=false)
    public BusinessServiceEntity getChild() {
        return child;
    }

    @Transient
    @Override
    public Set<String> getReductionKeys() {
        return Sets.newHashSet();
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("super", super.toString())
                .add("child", child == null ? null : child.getId())
                .toString();
    }

    @Override
    public boolean equalsDefinition(BusinessServiceEdgeEntity other) {
        boolean equalsSuper = super.equalsDefinition(other);
        if (equalsSuper) {
            return Objects.equals(child.getId(), ((BusinessServiceChildEdgeEntity) other).getChild().getId());
        }
        return false;
    }

    @Override
    public <T> T accept(EdgeEntityVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
