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
package org.opennms.features.topology.plugins.topo.bsm.browsers;

import java.util.List;
import java.util.Objects;

import org.opennms.core.criteria.Criteria;
import org.opennms.features.topology.api.browsers.OnmsContainerDatasource;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.vaadin.core.TransactionAwareBeanProxyFactory;

public class BusinessServiceContainerDatasource implements OnmsContainerDatasource<BusinessService, Long> {

    private BusinessServiceManager businessServiceManager;
    private final TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory;

    public BusinessServiceContainerDatasource(TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory) {
        this.transactionAwareBeanProxyFactory = transactionAwareBeanProxyFactory;
    }

    public void setBusinessServiceManager(BusinessServiceManager businessServiceManager) {
        this.businessServiceManager = transactionAwareBeanProxyFactory.createProxy(Objects.requireNonNull(businessServiceManager));
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Cannot add new items to this container");
    }

    @Override
    public void delete(Long itemId) {
        businessServiceManager.deleteBusinessService(businessServiceManager.getBusinessServiceById(itemId));
    }

    @Override
    public List<BusinessService> findMatching(Criteria criteria) {
        return businessServiceManager.findMatching(criteria);
    }

    @Override
    public int countMatching(Criteria criteria) {
        return businessServiceManager.countMatching(criteria);
    }

    @Override
    public BusinessService createInstance(Class<BusinessService> itemClass) {
        return businessServiceManager.createBusinessService();
    }
}
