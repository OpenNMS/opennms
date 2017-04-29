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

package org.opennms.web.rest.v2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.search.SearchCondition;
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.ApplicationStatusEntity;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.alarm.AlarmSummary;
import org.opennms.web.rest.v2.model.ApplicationDTO;
import org.opennms.web.rest.v2.model.BusinessServiceDTO;
import org.opennms.web.rest.v2.model.SeveritySearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component("statusBoxRestService")
@Path("status")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class StatusRestService {

    public static class ApplicationSummary {
        private OnmsApplication application;
        private OnmsSeverity severity;

        public ApplicationSummary(OnmsApplication application, OnmsSeverity severity) {
            this.application = application;
            this.severity = severity;
        }

        public OnmsApplication getApplication() {
            return application;
        }

        public OnmsSeverity getSeverity() {
            return severity;
        }

    }

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private OutageDao outageDao;

    @Autowired
    private AlarmDao alarmDao;

    @Autowired
    private BusinessServiceManager businessServiceManager;

    @Autowired
    private ApplicationDao applicationDao;

    @GET
    @Path("/summary/nodes")
    public List<Object[]> getNodeStatus() {
        final List<AlarmSummary> currentNodeAlarmSummaries = alarmDao.getNodeAlarmSummaries();
        final Map<OnmsSeverity, Long> severityMap = currentNodeAlarmSummaries.stream().collect(
                Collectors.groupingBy(summary -> summary.getMaxSeverity(), Collectors.counting()));

        // update normal severity
        final long nodeCount = nodeDao.countAll();
        final long nodesWithSeverityCount = severityMap.values().stream().count();
        final long normalCount = nodeCount - nodesWithSeverityCount + severityMap.getOrDefault(OnmsSeverity.NORMAL, 0L);
        severityMap.put(OnmsSeverity.NORMAL, normalCount);
        return convert(enrich(severityMap));
    }

    @GET
    @Path("/summary/applications")
    public List<Object[]> getApplicationStatus() {
        final List<ApplicationSummary> summaryList = getApplicationSummary();
        final Map<OnmsSeverity, Long> severityMap = summaryList.stream().collect(
                Collectors.groupingBy(summary -> summary.getSeverity(), Collectors.counting()));

        // update normal severity
        final long applicationCount = applicationDao.countAll();
        final long applicationWithSeverityCount = severityMap.values().stream().count();
        final long normalCount = applicationCount - applicationWithSeverityCount + severityMap.getOrDefault(OnmsSeverity.NORMAL, 0L);
        severityMap.put(OnmsSeverity.NORMAL, normalCount);

        return convert(enrich(severityMap));
    }

    private List<ApplicationSummary> getApplicationSummary() {
        return getApplicationSummary(applicationDao.findAll());
    }

    private List<ApplicationSummary> getApplicationSummary(List<OnmsApplication> applications) {
        // Applications do not have a alarm mapping, so we group all alarms by node id, service type and ip address
        // as those define the status of the application
        final List<ApplicationStatusEntity> alarmStatusList = applicationDao.getAlarmStatus();

        // Calculate status for application
        final List<ApplicationSummary> summaryList = new ArrayList<>();
        for (OnmsApplication application : applications) {
            final List<ApplicationStatusEntity> statusList = new ArrayList<>();
            for (OnmsMonitoredService eachService : application.getMonitoredServices()) {
                ApplicationStatusEntity.Key key = new ApplicationStatusEntity.Key(eachService.getNodeId(), eachService.getServiceType(), eachService.getIpAddress());
                alarmStatusList.stream().filter(s -> s.getKey().equals(key)).collect(Collectors.toList()).forEach(s -> statusList.add(s));
            }

            // We have determined all severities from all ip services, now get the max severity
            Optional<ApplicationStatusEntity> maxSeverity = statusList.stream().reduce((statusEntity1, statusEntity2) -> {
                if (statusEntity1.getSeverity().isGreaterThan(statusEntity2.getSeverity())) {
                    return statusEntity1;
                }
                return statusEntity2;
            });
            if (maxSeverity.isPresent()) {
                summaryList.add(new ApplicationSummary(application, maxSeverity.get().getSeverity()));
            }
        }

        // Merge with no status
        final List<OnmsApplication> applicationsWithStatus = summaryList.stream().map(s -> s.getApplication()).collect(Collectors.toList());
        final List<OnmsApplication> applicationsWithoutStatus = applications.stream().filter(a -> !applicationsWithStatus.contains(a)).collect(Collectors.toList());
        applicationsWithoutStatus.stream().forEach(a -> summaryList.add(new ApplicationSummary(a, OnmsSeverity.NORMAL)));
        return summaryList;
    }

    @GET
    @Path("/summary/business-services")
    public List<Object[]> getBusinessServiceStatus() {
        final Map<OnmsSeverity, Long> severityMap = businessServiceManager.getAllBusinessServices()
                .stream()
                .collect(
                    Collectors.groupingBy(b -> {
                        final Status operationalStatus = b.getOperationalStatus();
                        final OnmsSeverity severity = OnmsSeverity.get(operationalStatus.getLabel());
                        return severity;
                    }, Collectors.counting()));
        return convert(enrich(severityMap));
    }

    private Map<OnmsSeverity, Long> enrich(Map<OnmsSeverity, Long> severityMap) {
        severityMap.putIfAbsent(OnmsSeverity.NORMAL, 0L);
        severityMap.putIfAbsent(OnmsSeverity.WARNING, 0L);
        severityMap.putIfAbsent(OnmsSeverity.MINOR, 0L);
        severityMap.putIfAbsent(OnmsSeverity.MAJOR, 0L);
        severityMap.putIfAbsent(OnmsSeverity.CRITICAL, 0L);
        return severityMap;
    }

    @GET
    @Path("/summary/outages")
    public List<Object[]> getOutageStatus() {
        long outageCount = outageDao.countOutagesByNode();
        long normalCount = nodeDao.countAll() - outageCount;
        final Map<OnmsSeverity, Long> severityMap = new HashMap<>();
        severityMap.put(OnmsSeverity.NORMAL, normalCount);
        severityMap.put(OnmsSeverity.CRITICAL, outageCount);
        return convert(enrich(severityMap));
    }

    @GET
    @Path("/applications")
    public List<ApplicationDTO> getApplications(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
        // Remove the severity from orderBy because applications do not have a severity
        final MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        final String orderSeverity = getOrderBySeverityAndRemoveIfExisting(queryParameters);

        // Build criteria
        final CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsApplication.class);
        AbstractDaoRestService.applyLimitOffsetOrderBy(queryParameters, criteriaBuilder);

        // Query and apply filters
        final List<OnmsApplication> applications = applicationDao.findMatching(criteriaBuilder.toCriteria());
        List<ApplicationSummary> applicationSummaries = getApplicationSummary(applications);
        if (!applicationSummaries.isEmpty()) {
            final Predicate<? super ApplicationSummary> filter = getApplicationFilter(searchContext);
            applicationSummaries = applicationSummaries.stream().filter(filter).collect(Collectors.toList());
        }

        final List<ApplicationDTO> collect = applicationSummaries.stream().map(applicationSummary -> {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setId(applicationSummary.getApplication().getId());
            dto.setName(applicationSummary.getApplication().getName());
            dto.setStatus(applicationSummary.getSeverity());
            return dto;
        }).collect(Collectors.toList());

        // sort if required
        if (orderSeverity != null) {
            Comparator<ApplicationDTO> comparator = Comparator.comparing(ApplicationDTO::getStatus);
            if ("desc".equals(orderSeverity)) {
                comparator = comparator.reversed();
            }
            collect.sort(comparator);
        }
        return collect;
    }

    private String getOrderBySeverityAndRemoveIfExisting(MultivaluedMap<String, String> queryParameters) {
        String orderBy = queryParameters.getFirst("orderBy");
        String order = queryParameters.getFirst("order");
        if (orderBy != null && orderBy.equalsIgnoreCase("severity")) {
            queryParameters.remove("orderBy");
            queryParameters.remove("order");
            return order;
        }
        return null;
    }

    private Predicate<? super ApplicationSummary> getApplicationFilter(final SearchContext searchContext) {
        if (searchContext != null && !Strings.isNullOrEmpty(searchContext.getSearchExpression())) {
            final SearchCondition<SeveritySearchRequest> search = searchContext.getCondition(SeveritySearchRequest.class);
            if (search != null && search.getCondition().getSeverity() != null) {
                final OnmsSeverity searchSeverity = OnmsSeverity.get(search.getCondition().getSeverity());
                switch (search.getConditionType()) {
                    case EQUALS:
                        return applicationSummary -> applicationSummary.getSeverity().equals(searchSeverity);
                    case GREATER_OR_EQUALS:
                        return applicationSummary -> applicationSummary.getSeverity().isGreaterThanOrEqual(searchSeverity);
                    case LESS_OR_EQUALS:
                        return applicationSummary -> applicationSummary.getSeverity().isLessThanOrEqual(searchSeverity);
                    case NOT_EQUALS:
                        return applicationSummary -> !applicationSummary.getSeverity().equals(searchSeverity);
                }
            }
        }
        // Include all
        return (Predicate<ApplicationSummary>) applicationSummary -> true;
    }

    @GET
    @Path("/business-services")
    public List<BusinessServiceDTO> getBusinessServices() {
        final List<BusinessServiceDTO> businessServices = businessServiceManager.getAllBusinessServices()
                .stream()
                .map(b -> {
                    final BusinessServiceDTO dto = new BusinessServiceDTO();
                    dto.setId(b.getId());
                    dto.setName(b.getName());
                    dto.setStatus(OnmsSeverity.get(b.getOperationalStatus().getLabel()));
                    return dto;
                }).collect(Collectors.toList());
        return businessServices;
    }

    private static List<Object[]> convert(Map<OnmsSeverity, Long> input) {
        return input.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(e -> new Object[]{e.getKey().getLabel(), e.getValue()})
                .collect(Collectors.toList());
    }
}
