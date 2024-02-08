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

import org.opennms.netmgt.bsm.persistence.api.ApplicationEdgeEntity;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.internal.ApplicationImpl;
import org.opennms.netmgt.bsm.service.model.Application;
import org.opennms.netmgt.bsm.service.model.edge.ApplicationEdge;
import org.opennms.netmgt.bsm.service.model.edge.EdgeVisitor;

import com.google.common.base.MoreObjects;

public class ApplicationEdgeImpl extends AbstractEdge<ApplicationEdgeEntity> implements ApplicationEdge {

    public ApplicationEdgeImpl(BusinessServiceManager manager, ApplicationEdgeEntity entity) {
        super(manager, entity);
    }

    @Override
    public Application getApplication() {
        return new ApplicationImpl(getManager(), getEntity().getApplication());
    }

    @Override
    public void setApplication(Application application) {
        getEntity().setApplication(((ApplicationImpl)application).getEntity());
    }

    @Override
    public Set<String> getReductionKeys() {
        return getEntity().getReductionKeys();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }

    @Override
    public String getFriendlyName() {
        return getEntity().getApplication().getName();
    }

    @Override
    public <T> T accept(EdgeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}