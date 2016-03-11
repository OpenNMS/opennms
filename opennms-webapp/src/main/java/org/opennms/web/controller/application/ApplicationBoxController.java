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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.controller.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.ApplicationStatusEntity;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsSeverity;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class ApplicationBoxController extends AbstractController implements InitializingBean {

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

    public static final int DEFAULT_ROW_COUNT = 10;
    private ApplicationDao applicationDao;
    private String m_successView;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final int numberOfRows = Integer.getInteger("opennms.applicationsWithProblems.count", DEFAULT_ROW_COUNT);

        // Applications do not have a alarm mapping, so we group all alarms by node id, service type and ip address
        // as those define the status of the application
        final List<ApplicationStatusEntity> alarmStatusList = applicationDao.getAlarmStatus();

        // Calculate status for application
        List<ApplicationSummary> summaryList = new ArrayList<>();
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

        // Define if there is a "more"
        boolean more = summaryList.size() - numberOfRows > 0;
        if (summaryList.size() > numberOfRows) {
            summaryList = summaryList.subList(0, numberOfRows);
        }

        // Sort
        summaryList.sort((s1, s2) -> -1 * s1.getSeverity().compareTo(s2.getSeverity())); // desc sort

        // Prepare Model
        ModelAndView modelAndView = new ModelAndView(m_successView);
        modelAndView.addObject("more", more);
        modelAndView.addObject("summaries", summaryList);
        return modelAndView;
    }

    public void setSuccessView(String successView) {
        m_successView = successView;
    }

    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    @Override
    public void afterPropertiesSet() {
        Objects.requireNonNull(m_successView, "successView must be set");
        Objects.requireNonNull(applicationDao, "applicationDao must be set");
    }
}
