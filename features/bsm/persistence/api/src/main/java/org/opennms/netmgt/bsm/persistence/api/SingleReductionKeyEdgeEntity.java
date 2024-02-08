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
package org.opennms.netmgt.bsm.persistence.api;

import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import com.google.common.collect.Sets;

@Entity
@Table(name = "bsm_service_reductionkeys",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id", "reductionkey"}))
@PrimaryKeyJoinColumn(name="id")
@DiscriminatorValue("reductionkeys")
public class SingleReductionKeyEdgeEntity extends BusinessServiceEdgeEntity {

    private String reductionKey;
    private String m_friendlyName;

    public void setReductionKey(String reductionKey) {
        this.reductionKey = reductionKey;
    }

    @Column(name = "reductionkey", nullable = false)
    public String getReductionKey() {
        return reductionKey;
    }

    @Override
    @Transient
    public Set<String> getReductionKeys() {
        return Sets.newHashSet(reductionKey);
    }

    @Column(name="friendlyname", nullable = true)
    @Size(min = 0, max = 30)
    public String getFriendlyName() {
        return m_friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        m_friendlyName = friendlyName;
    }

    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("super", super.toString())
                .add("reductionKey", reductionKey)
                .toString();
    }

    @Override
    public boolean equalsDefinition(BusinessServiceEdgeEntity other) {
        boolean equalsSuper = super.equalsDefinition(other);
        if (equalsSuper) {
            return Objects.equals(reductionKey, ((SingleReductionKeyEdgeEntity) other).reductionKey) &&
                   Objects.equals(m_friendlyName, ((SingleReductionKeyEdgeEntity) other).m_friendlyName);
        }
        return false;
    }

    @Override
    public <T> T accept(EdgeEntityVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
