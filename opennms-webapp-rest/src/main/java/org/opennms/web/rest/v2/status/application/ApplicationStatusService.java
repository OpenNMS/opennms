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

package org.opennms.web.rest.v2.status.application;

import java.util.List;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.ApplicationStatus;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.rest.support.CriteriaBuilderUtils;
import org.opennms.web.rest.support.QueryParameters;
import org.opennms.web.rest.v2.status.AbstractStatusService;
import org.opennms.web.rest.v2.status.StatusSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationStatusService extends AbstractStatusService<ApplicationDTO> {

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private AlarmDao alarmDao;

    protected CriteriaBuilder getCriteriaBuilder(QueryParameters queryParameters) {
        return CriteriaBuilderUtils.buildFrom(OnmsApplication.class, queryParameters);
    }

    @Override
    protected int countMatching(Criteria criteria) {
        return applicationDao.countMatching(criteria);
    }

    @Override
    public StatusSummary getSummary() {
        final List<ApplicationStatus> statusList = getApplicationStatus();
        final List<OnmsSeverity> severityList = statusList.stream().map(status -> status.getSeverity()).collect(Collectors.toList());
        final long applicationCount = applicationDao.countAll();
        return new StatusSummary(severityList, applicationCount);
    }

    @Override
    protected List<ApplicationDTO> findMatching(CriteriaBuilder criteriaBuilder) {
        final List<OnmsApplication> applications = applicationDao.findMatching(criteriaBuilder.toCriteria());
        List<ApplicationStatus> applicationStatus = getApplicationStatus(applications);

        // Convert to ApplicationWithStatus objects
        final List<ApplicationDTO> collect = applicationStatus.stream().map(status -> {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setId(status.getApplication().getId());
            dto.setName(status.getApplication().getName());
            dto.setSeverity(status.getSeverity());
            return dto;
        }).collect(Collectors.toList());

        return collect;
    }

    private List<ApplicationStatus> getApplicationStatus() {
        return getApplicationStatus(applicationDao.findAll());
    }

    private List<ApplicationStatus> getApplicationStatus(List<OnmsApplication> applications) {
        return applicationDao.getApplicationStatus(applications);
    }

}
