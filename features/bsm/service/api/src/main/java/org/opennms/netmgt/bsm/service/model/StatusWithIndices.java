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
