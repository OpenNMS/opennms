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
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.opennms.netmgt.dao.util.ReductionKeyHelper;
import org.opennms.netmgt.model.OnmsMonitoredService;

@Entity
@Table(name = "bsm_service_ifservices")
@PrimaryKeyJoinColumn(name="id")
public class IPServiceEdgeEntity extends BusinessServiceEdgeEntity {

    private OnmsMonitoredService m_ipService;

    private String m_friendlyName;

    // NOTE: When we use @Column on this field, Hibernate attempts to serialize the objects as a byte array
    // Instead, we resort to use @ManyToOne
    @ManyToOne(optional=false)
    @JoinColumn(name="ifserviceid", nullable=false)
    public OnmsMonitoredService getIpService() {
        return m_ipService;
    }

    public void setIpService(OnmsMonitoredService ipService) {
        m_ipService = ipService;
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
    @Transient
    public Set<String> getReductionKeys() {
        return ReductionKeyHelper.getReductionKeys(m_ipService);
    }

    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("super", super.toString())
                .add("ipService", m_ipService)
                .toString();
    }

    @Override
    public boolean equalsDefinition(BusinessServiceEdgeEntity other) {
        boolean equalsSuper = super.equalsDefinition(other);
        if (equalsSuper) {
            return Objects.equals(m_ipService, ((IPServiceEdgeEntity) other).m_ipService) &&
                   Objects.equals(m_friendlyName, ((IPServiceEdgeEntity) other).m_friendlyName);
        }
        return false;
    }

    @Override
    public <T> T accept(EdgeEntityVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
