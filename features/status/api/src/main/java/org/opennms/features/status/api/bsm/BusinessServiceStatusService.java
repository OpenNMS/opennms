/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.status.api.bsm;

import java.util.List;
import java.util.stream.Collectors;

import org.opennms.features.status.api.Query;
import org.opennms.features.status.api.StatusEntity;
import org.opennms.features.status.api.StatusEntityWrapper;
import org.opennms.features.status.api.StatusSummary;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.BusinessServiceSearchCriteriaBuilder;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.utils.QueryParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BusinessServiceStatusService {

    @Autowired
    private BusinessServiceManager businessServiceManager;

    public int count(Query query) {
        final BusinessServiceSearchCriteriaBuilder criteria = buildFrom(query);
        criteria.prepareForCounting();

        final List<BusinessService> services = criteria.apply(businessServiceManager, businessServiceManager.getAllBusinessServices());
        return services.size();
    }

    public StatusSummary getSummary() {
        final List<BusinessService> businessServices = businessServiceManager.getAllBusinessServices();
        final List<OnmsSeverity> severityList = businessServices.stream().map(bs -> OnmsSeverity.get(bs.getOperationalStatus().getLabel())).collect(Collectors.toList());
        final long totalCount = businessServices.size();
        return new StatusSummary(severityList, totalCount);
    }

    public List<StatusEntity<BusinessService>> getStatus(Query query) {
        final BusinessServiceSearchCriteriaBuilder criteria = buildFrom(query);
        final List<BusinessService> services = criteria.apply(businessServiceManager, businessServiceManager.getAllBusinessServices());
        final List<StatusEntity<BusinessService>> mappedServices = services
                .stream()
                .map(eachService -> new StatusEntityWrapper<>(eachService, OnmsSeverity.get(eachService.getOperationalStatus().getLabel())))
                .collect(Collectors.toList());
        return mappedServices;
    }

    private BusinessServiceSearchCriteriaBuilder buildFrom(Query query) {
        final BusinessServiceSearchCriteriaBuilder criteriaBuilder = new BusinessServiceSearchCriteriaBuilder();

        if (query.getSeverityFilter() != null && !query.getSeverityFilter().getSeverities().isEmpty()) {
            final List<Status> statusList = query.getSeverityFilter().getSeverities().stream().map(eachSeverity -> Status.of(eachSeverity.name())).collect(Collectors.toList());
            criteriaBuilder.inSeverity(statusList);
        } else {
            criteriaBuilder.greaterOrEqualSeverity(Status.NORMAL);
        }

        if (query.getParameters().getOrder() != null) {
            final QueryParameters.Order order = query.getParameters().getOrder();
            criteriaBuilder.order(order.getColumn(), !order.isDesc());
        }

        if (query.getParameters().getLimit() != null) {
            criteriaBuilder.limit(query.getParameters().getLimit());
        }
        if (query.getParameters().getOffset() != null) {
            criteriaBuilder.offset(query.getParameters().getOffset());
        }

        return criteriaBuilder;
    }
}
