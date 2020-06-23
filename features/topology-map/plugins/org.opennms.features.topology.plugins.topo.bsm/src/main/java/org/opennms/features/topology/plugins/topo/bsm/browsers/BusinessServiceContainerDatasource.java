/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

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
