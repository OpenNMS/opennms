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
