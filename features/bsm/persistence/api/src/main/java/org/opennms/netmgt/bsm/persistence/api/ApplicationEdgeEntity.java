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

import org.opennms.netmgt.dao.util.ReductionKeyHelper;
import org.opennms.netmgt.model.OnmsApplication;

import com.google.common.base.MoreObjects;

@Entity
@Table(name = "bsm_service_applications")
@PrimaryKeyJoinColumn(name="id")
@DiscriminatorValue("applications")
public class ApplicationEdgeEntity extends BusinessServiceEdgeEntity {

    private OnmsApplication m_application;

    @ManyToOne(optional=false)
    @JoinColumn(name="applicationid", nullable=false)
    public OnmsApplication getApplication() {
        return m_application;
    }

    public void setApplication(OnmsApplication application) {
        m_application = application;
    }

    @Override
    @Transient
    public Set<String> getReductionKeys() {
        return ReductionKeyHelper.getReductionKeys(m_application);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("m_application", m_application)
                .toString();
    }

    @Override
    public boolean equalsDefinition(BusinessServiceEdgeEntity other) {
        boolean equalsSuper = super.equalsDefinition(other);
        if (equalsSuper) {
            return Objects.equals(m_application, ((ApplicationEdgeEntity) other).m_application);
        }
        return false;
    }

    @Override
    public <T> T accept(EdgeEntityVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
