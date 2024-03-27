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

import com.google.common.base.Preconditions;

@Entity
@DiscriminatorValue("threshold")
public class ThresholdEntity extends AbstractReductionFunctionEntity {

    @Column(name="threshold", nullable=false)    
    private float m_threshold;

    public ThresholdEntity() {

    }

    public ThresholdEntity(float threshold) {
        setThreshold(threshold);
    }

    public void setThreshold(float threshold) {
        Preconditions.checkArgument(threshold > 0, "threshold must be strictly positive");
        Preconditions.checkArgument(threshold <= 1, "threshold must be less or equal to 1");
        m_threshold = threshold;
    }

    public float getThreshold() {
        return m_threshold;
    }

    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("threshold", m_threshold)
                .toString();
    }

    @Override
    public <T> T accept(ReductionFunctionEntityVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
