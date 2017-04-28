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

package org.opennms.web.rest.v1;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("statusBoxRestService")
@Path("status-box")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class StatusBoxRestService {

    public static class StatusSummaryDTO {

    }


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
    @Path("/nodes")
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
    @Path("/applications")
    public List<Object[]> getApplicationStatus() {
        // Applications do not have a alarm mapping, so we group all alarms by node id, service type and ip address
        // as those define the status of the application
        final List<ApplicationStatusEntity> alarmStatusList = applicationDao.getAlarmStatus();

        // Calculate status for application
        final List<ApplicationSummary> summaryList = new ArrayList<>();
        for (OnmsApplication application : applicationDao.findAll()) {
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
        final Map<OnmsSeverity, Long> severityMap = summaryList.stream().collect(
                Collectors.groupingBy(summary -> summary.getSeverity(), Collectors.counting()));

        // update normal severity
        final long applicationCount = applicationDao.countAll();
        final long applicationWithSeverityCount = severityMap.values().stream().count();
        final long normalCount = applicationCount - applicationWithSeverityCount + severityMap.getOrDefault(OnmsSeverity.NORMAL, 0L);
        severityMap.put(OnmsSeverity.NORMAL, normalCount);

        return convert(enrich(severityMap));
    }

    @GET
    @Path("/business-services")
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
    @Path("/outages")
    public List<Object[]> getOutageStatus() {
        long outageCount = outageDao.countOutagesByNode();
        long normalCount = nodeDao.countAll() - outageCount;
        final Map<OnmsSeverity, Long> severityMap = new HashMap<>();
        severityMap.put(OnmsSeverity.NORMAL, normalCount);
        severityMap.put(OnmsSeverity.CRITICAL, outageCount);
        return convert(enrich(severityMap));
    }

    private static List<Object[]> convert(Map<OnmsSeverity, Long> input) {
        return input.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(e -> new Object[]{e.getKey().getLabel(), e.getValue()})
                .collect(Collectors.toList());
    }
}
