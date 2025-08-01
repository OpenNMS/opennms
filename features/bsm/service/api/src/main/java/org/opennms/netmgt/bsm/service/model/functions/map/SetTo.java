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
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("severity", getStatus())
                .toString();
    }

    @Override
    public <T> T accept(MapFunctionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
