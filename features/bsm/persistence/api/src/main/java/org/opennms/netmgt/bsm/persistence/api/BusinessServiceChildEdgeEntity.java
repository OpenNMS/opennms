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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.google.common.collect.Sets;

@Entity
@Table(name = "bsm_service_children")
@PrimaryKeyJoinColumn(name="id")
@DiscriminatorValue(value="children")
public class BusinessServiceChildEdgeEntity extends BusinessServiceEdgeEntity {

    // The Business Service Entity where the parent points to (child relationship)
    private BusinessServiceEntity child;

    public void setChild(BusinessServiceEntity child) {
        this.child = child;
    }

    @ManyToOne(optional=false)
    @JoinColumn(name="bsm_service_child_id", nullable=false)
    public BusinessServiceEntity getChild() {
        return child;
    }

    @Transient
    @Override
    public Set<String> getReductionKeys() {
        return Sets.newHashSet();
    }

    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("super", super.toString())
                .add("child", child == null ? null : child.getId())
                .toString();
    }

    @Override
    public boolean equalsDefinition(BusinessServiceEdgeEntity other) {
        boolean equalsSuper = super.equalsDefinition(other);
        if (equalsSuper) {
            return Objects.equals(child.getId(), ((BusinessServiceChildEdgeEntity) other).getChild().getId());
        }
        return false;
    }

    @Override
    public <T> T accept(EdgeEntityVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
