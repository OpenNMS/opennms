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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.bsm.simulate;

import java.util.Objects;

import org.opennms.netmgt.bsm.service.model.Status;

/**
 * This criteria is used to alter the severity of a specific reduction key.
 *
 * @author jwhite
 */
public class SetStatusToCriteria extends SimulationCriteria {

    private Status status;
    private final String reductionKey;

    public SetStatusToCriteria(String reductionKey, Status status) {
        this.reductionKey = Objects.requireNonNull(reductionKey);
        this.status = status;
    }

    public String getReductionKey() {
        return reductionKey;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reductionKey, status);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SetStatusToCriteria other = (SetStatusToCriteria) obj;
        return Objects.equals(this.reductionKey, other.reductionKey) &&
                Objects.equals(this.status, other.status);
    }
}
