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

import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.edge.ChildEdge;
import org.opennms.netmgt.bsm.service.model.edge.EdgeVisitor;
import org.opennms.netmgt.bsm.service.model.functions.map.Identity;

import com.google.common.collect.Sets;

public class MockChildEdge extends AbstractMockEdge implements ChildEdge {
    private BusinessService m_businessService;

    public MockChildEdge(long id, BusinessService businessService) {
        super(id, new Identity());
        m_businessService = businessService;
    }

    @Override
    public Set<String> getReductionKeys() {
        return Sets.newHashSet();
    }

    @Override
    public BusinessService getChild() {
        return m_businessService;
    }

    @Override
    public void setChild(BusinessService child) {
        m_businessService = child;
    }

    @Override
    public String toString() {
        return String.format("MockChildEdge[id=%d, businessService=%s]", getId(), m_businessService);
    }

    @Override
    public String getFriendlyName() {
        return null;
    }

    @Override
    public <T> T accept(EdgeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
