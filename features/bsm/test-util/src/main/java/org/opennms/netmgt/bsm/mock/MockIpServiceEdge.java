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
package org.opennms.netmgt.bsm.mock;

import java.util.Set;

import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.edge.EdgeVisitor;
import org.opennms.netmgt.bsm.service.model.edge.IpServiceEdge;
import org.opennms.netmgt.bsm.service.model.functions.map.Identity;

public class MockIpServiceEdge extends AbstractMockEdge implements IpServiceEdge {

    private IpService m_ipService;

    private String m_friendlyName;

    public MockIpServiceEdge(long id, IpService ipService, String friendlyName) {
        super(id, new Identity());
        m_friendlyName = friendlyName;
        m_ipService = ipService;
    }

    @Override
    public IpService getIpService() {
        return m_ipService;
    }

    @Override
    public Set<String> getReductionKeys() {
        return m_ipService.getReductionKeys();
    }

    @Override
    public String getFriendlyName() {
        return m_friendlyName;
    }

    @Override
    public void setIpService(IpService ipService) {
        m_ipService = ipService;
    }

    @Override
    public void setFriendlyName(String friendlyName) {
        m_friendlyName = friendlyName;
    }

    @Override
    public <T> T accept(EdgeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
