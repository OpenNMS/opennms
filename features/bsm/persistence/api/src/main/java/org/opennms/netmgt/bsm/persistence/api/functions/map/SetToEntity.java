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
        return com.google.common.base.MoreObjects.toStringHelper(this)
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
