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

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.opennms.netmgt.dao.util.ReductionKeyHelper;
import org.opennms.netmgt.model.OnmsApplication;

import com.google.common.base.MoreObjects;

@Entity
@Table(name = "bsm_service_applications")
@PrimaryKeyJoinColumn(name="id")
public class ApplicationEdgeEntity extends BusinessServiceEdgeEntity {

    private OnmsApplication m_application;

    @ManyToOne(optional=false)
    @JoinColumn(name="applicationid", nullable=false)
    public OnmsApplication getApplication() {
        return m_application;
    }

    public void setApplication(OnmsApplication application) {
        m_application = application;
    }

    @Override
    @Transient
    public Set<String> getReductionKeys() {
        return ReductionKeyHelper.getReductionKeys(m_application);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("m_application", m_application)
                .toString();
    }

    @Override
    public boolean equalsDefinition(BusinessServiceEdgeEntity other) {
        boolean equalsSuper = super.equalsDefinition(other);
        if (equalsSuper) {
            return Objects.equals(m_application, ((ApplicationEdgeEntity) other).m_application);
        }
        return false;
    }

    @Override
    public <T> T accept(EdgeEntityVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
