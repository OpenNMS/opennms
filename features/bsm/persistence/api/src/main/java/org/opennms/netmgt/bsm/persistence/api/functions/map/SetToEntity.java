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

package org.opennms.netmgt.bsm.persistence.api.functions.map;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.opennms.netmgt.model.OnmsSeverity;

@Entity
@DiscriminatorValue(value="set-to")
public class SetToEntity extends AbstractMapFunctionEntity {

    @Column(name="severity", nullable=false)    
    private Integer m_severity;

    public SetToEntity() {

    }

    public SetToEntity(Integer severity) {
        m_severity = Objects.requireNonNull(severity);
    }

    public void setSeverity(OnmsSeverity severity) {
        m_severity = Objects.requireNonNull(severity).getId();
    }

    public OnmsSeverity getSeverity() {
        if (m_severity == null) {
            return null;
        } else {
            return OnmsSeverity.get(m_severity);
        }
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("id", getId())
                .add("severity", getSeverity())
                .toString();
    }

    @Override
    public <T extends AbstractMapFunctionEntity> boolean equalsDefinition(T other) {
        boolean equalsSuper = super.equalsDefinition(other);
        if (equalsSuper) {
            return Objects.equals(m_severity, ((SetToEntity)other).m_severity);
        }
        return false;
    }

    @Override
    public <T> T accept(MapFunctionEntityVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
