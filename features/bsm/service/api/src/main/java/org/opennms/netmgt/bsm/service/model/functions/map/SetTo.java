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

package org.opennms.netmgt.bsm.service.model.functions.map;

import java.util.Objects;
import java.util.Optional;

import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.functions.annotations.Function;
import org.opennms.netmgt.bsm.service.model.functions.annotations.Parameter;

@Function(name="SetTo", description = "Sets the status to a defined value")
public class SetTo implements MapFunction {

    @Parameter(key="status", description="The status value to set the status to")
    private Status m_severity;

    public void setStatus(Status severity) {
        m_severity = Objects.requireNonNull(severity);
    }

    public Status getStatus() {
        return m_severity;
    }

    @Override
    public Optional<Status> map(Status source) {
        return Optional.of(getStatus());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SetTo other = (SetTo) obj;
        return Objects.equals(m_severity, other.m_severity) && super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_severity);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("severity", getStatus())
                .toString();
    }

    @Override
    public <T> T accept(MapFunctionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
