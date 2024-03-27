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
package org.opennms.netmgt.bsm.persistence.api.functions.reduce;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value="exponential-propagation")
public class ExponentialPropagationEntity extends AbstractReductionFunctionEntity {

    @Column(name="base", nullable=false)
    private double m_base;

    public ExponentialPropagationEntity() {
    }

    public ExponentialPropagationEntity(double base) {
        setBase(base);
    }

    public void setBase(double base) {
        m_base = base;
    }

    public double getBase() {
        return m_base;
    }

    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("base", m_base)
                .toString();
    }

    @Override
    public <T> T accept(ReductionFunctionEntityVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
