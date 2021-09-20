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

package org.opennms.features.status.api.application;

import java.util.List;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.status.api.AbstractStatusService;
import org.opennms.features.status.api.Query;
import org.opennms.features.status.api.StatusEntity;
import org.opennms.features.status.api.StatusEntityWrapper;
import org.opennms.features.status.api.StatusSummary;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.ApplicationStatus;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.utils.CriteriaBuilderUtils;
import org.opennms.web.utils.QueryParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationStatusService extends AbstractStatusService<OnmsApplication, Query> {

    @Autowired
    private ApplicationDao applicationDao;

    protected CriteriaBuilder getCriteriaBuilder(QueryParameters queryParameters) {
        return CriteriaBuilderUtils.buildFrom(OnmsApplication.class, queryParameters);
    }

    @Override
    protected int countMatching(Criteria criteria) {
        return applicationDao.countMatching(criteria);
    }

    public StatusSummary getSummary() {
        final List<ApplicationStatus> statusList = getApplicationStatus();
        final List<OnmsSeverity> severityList = statusList.stream().map(status -> status.getSeverity()).collect(Collectors.toList());
        final long applicationCount = applicationDao.countAll();
        return new StatusSummary(severityList, applicationCount);
    }

    @Override
    protected List<StatusEntity<OnmsApplication>> findMatching(Query query, CriteriaBuilder criteriaBuilder) {
        final List<OnmsApplication> applications = applicationDao.findMatching(criteriaBuilder.toCriteria());
        List<ApplicationStatus> applicationStatus = getApplicationStatus(applications);

        // Convert to ApplicationWithStatus wrapper
        final List<StatusEntity<OnmsApplication>> collect = applicationStatus
                .stream()
                .map(status -> new StatusEntityWrapper<>(status.getApplication(), status.getSeverity()))
                .collect(Collectors.toList());

        return collect;
    }

    private List<ApplicationStatus> getApplicationStatus() {
        return getApplicationStatus(applicationDao.findAll());
    }

    private List<ApplicationStatus> getApplicationStatus(List<OnmsApplication> applications) {
        return applicationDao.getApplicationStatus(applications);
    }

}
