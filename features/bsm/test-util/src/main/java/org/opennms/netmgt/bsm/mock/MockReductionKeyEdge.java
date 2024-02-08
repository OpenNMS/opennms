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

import org.opennms.netmgt.bsm.service.model.edge.EdgeVisitor;
import org.opennms.netmgt.bsm.service.model.edge.ReductionKeyEdge;
import org.opennms.netmgt.bsm.service.model.functions.map.Identity;

public class MockReductionKeyEdge extends AbstractMockEdge implements ReductionKeyEdge {

    private String friendlyName;

    public MockReductionKeyEdge(long id, String reductionKey, String friendlyName) {
        super(id, new Identity());
        setReductionKey(reductionKey);
        setFriendlyName(friendlyName);
    }

    @Override
    public String getReductionKey() {
        return getReductionKeys().isEmpty() ? null : getReductionKeys().iterator().next();
    }

    @Override
    public void setReductionKey(String reductionKey) {
        getReductionKeys().clear();
        getReductionKeys().add(reductionKey);
    }

    @Override
    public String getFriendlyName() {
        return friendlyName;
    }

    @Override
    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    @Override
    public <T> T accept(EdgeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
