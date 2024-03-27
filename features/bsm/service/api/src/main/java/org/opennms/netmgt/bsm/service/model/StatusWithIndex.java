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

import java.util.Objects;

public class StatusWithIndex {

    private final Status status;
    private final int index;

    public StatusWithIndex(Status status, int index) {
        this.status = Objects.requireNonNull(status);
        this.index = index;
    }

    public Status getStatus() {
        return status;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, index);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StatusWithIndex other = (StatusWithIndex) obj;
        return Objects.equals(this.status, other.status)
                && Objects.equals(this.index, other.index);
    }

    public String toString() {
        return String.format("StatusWithIndex[status=%s, index=%s]", status, index);
    }
}
