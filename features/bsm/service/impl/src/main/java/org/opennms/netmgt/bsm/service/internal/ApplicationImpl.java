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
package org.opennms.netmgt.bsm.service.internal;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.Application;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.dao.util.ReductionKeyHelper;
import org.opennms.netmgt.model.OnmsApplication;

import com.google.common.base.MoreObjects;

public class ApplicationImpl implements Application {

    private final BusinessServiceManager m_manager;

    private final OnmsApplication m_entity;

    public ApplicationImpl(final BusinessServiceManager manager,
                           final OnmsApplication entity) {
        this.m_manager = manager;
        this.m_entity = entity;
    }

    @Override
    public String getApplicationName() {
        return m_entity.getName();
    }

    public OnmsApplication getEntity() {
        return m_entity;
    }

    @Override
    public int getId() {
        return m_entity.getId();
    }

    @Override
    public Set<String> getReductionKeys() {
        return Collections.unmodifiableSet(ReductionKeyHelper.getReductionKeys(m_entity));
    }

    @Override
    public Set<IpService> getIpServices() {
        return Collections.unmodifiableSet(m_entity.getMonitoredServices().stream().map(e -> new IpServiceImpl(m_manager, e)).collect(Collectors.toSet()));
    }

    @Override
    public Set<Integer> getNodeIds() {
        return Collections.unmodifiableSet(m_entity.getMonitoredServices().stream().map(s -> s.getNodeId()).collect(Collectors.toSet()));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (getClass() != obj.getClass()) return false;
        final ApplicationImpl other = (ApplicationImpl) obj;
        return Objects.equals(getEntity(), other.getEntity());
    }

    @Override
    public int hashCode() {
        return getEntity().hashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("m_manager", m_manager)
                .add("m_entity", m_entity)
                .toString();
    }
}
