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
package org.opennms.netmgt.bsm.service.internal.edge;

import java.util.Set;

import org.opennms.netmgt.bsm.persistence.api.IPServiceEdgeEntity;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.internal.IpServiceImpl;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.edge.EdgeVisitor;
import org.opennms.netmgt.bsm.service.model.edge.IpServiceEdge;

public class IpServiceEdgeImpl extends AbstractEdge<IPServiceEdgeEntity> implements IpServiceEdge {

    public IpServiceEdgeImpl(BusinessServiceManager manager, IPServiceEdgeEntity entity) {
        super(manager, entity);
    }

    @Override
    public IpService getIpService() {
        return new IpServiceImpl(getManager(), getEntity().getIpService());
    }

    @Override
    public void setIpService(IpService ipService) {
        getEntity().setIpService(((IpServiceImpl)ipService).getEntity());
    }

    @Override
    public Set<String> getReductionKeys() {
        return getEntity().getReductionKeys();
    }

    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("parent", super.toString())
                .add("ipService", getIpService())
                .toString();
    }

    @Override
    public void setFriendlyName(String friendlyName) {
        getEntity().setFriendlyName(friendlyName);
    }

    @Override
    public String getFriendlyName() {
        return getEntity().getFriendlyName();
    }

    @Override
    public <T> T accept(EdgeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
