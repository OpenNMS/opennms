/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
