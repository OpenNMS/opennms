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
