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

package org.opennms.netmgt.bsm.service.model;

import java.util.List;
import java.util.Objects;

public class StatusWithIndices {

    private final Status status;
    private final List<Integer> indices;

    public StatusWithIndices(Status status, List<Integer> indices) {
        this.status = Objects.requireNonNull(status);
        this.indices = Objects.requireNonNull(indices);
    }

    public Status getStatus() {
        return status;
    }

    public List<Integer> getIndices() {
        return indices;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, indices);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StatusWithIndices other = (StatusWithIndices) obj;
        return Objects.equals(this.status, other.status)
                && Objects.equals(this.indices, other.indices);
    }

    @Override
    public String toString() {
        return String.format("StatusWithIndices[status=%s, indices=%s]", status, indices);
    }
}
