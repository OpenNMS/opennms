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

import java.util.Collections;
import java.util.Set;

import org.opennms.netmgt.bsm.persistence.api.BusinessServiceChildEdgeEntity;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.internal.BusinessServiceImpl;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.edge.ChildEdge;
import org.opennms.netmgt.bsm.service.model.edge.EdgeVisitor;

public class ChildEdgeImpl extends AbstractEdge<BusinessServiceChildEdgeEntity> implements ChildEdge {

    public ChildEdgeImpl(BusinessServiceManager manager, BusinessServiceChildEdgeEntity entity) {
        super(manager, entity);
    }

    @Override
    public BusinessService getChild() {
        return new BusinessServiceImpl(getManager(), getEntity().getChild());
    }

    @Override
    public Set<String> getReductionKeys() {
        return Collections.unmodifiableSet(getEntity().getReductionKeys());
    }

    @Override
    public void setChild(BusinessService child) {
        getEntity().setChild(((BusinessServiceImpl) child).getEntity());
    }

    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("parent", super.toString())
                .add("child", getChild() == null ? null : getChild().getId())
                .toString();
    }

    /**
     * Method implementation for the friendly name used in the topology UI. Since this value is not
     * used for child Business Services this method always returns <code>null</code>.
     *
     * @return always null
     * @see AbstractEdge#getFriendlyName()
     */
    @Override
    public String getFriendlyName() {
        return null;
    }

    @Override
    public <T> T accept(EdgeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
