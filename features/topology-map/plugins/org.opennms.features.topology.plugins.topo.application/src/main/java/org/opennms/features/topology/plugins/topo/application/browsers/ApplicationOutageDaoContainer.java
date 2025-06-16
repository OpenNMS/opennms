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
package org.opennms.features.topology.plugins.topo.application.browsers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.InRestriction;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.OnmsVaadinContainer;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;

public class ApplicationOutageDaoContainer extends OnmsVaadinContainer<ApplicationOutage, Integer> {
    private static final long serialVersionUID = 1L;

    private ApplicationDao applicationDao;

    public ApplicationOutageDaoContainer(OutageDao dao, ApplicationDao applicationDao) {
        super(ApplicationOutage.class, new ApplicationOutageDatasource(dao));
        this.applicationDao = applicationDao;
    }

    @Override
    protected Integer getId(ApplicationOutage bean) {
        return bean == null ? null : bean.getId();
    }

    @Override
    protected ContentType getContentType() {
        return ContentType.Application;
    }

    @Override
    protected void addAdditionalCriteriaOptions(Criteria criteria, Page page, boolean doOrder) {

        // filter out relevant selectedIds. They can be either of type application or service. This works since all ids
        // are unique.
        Collection<Integer> selectedIds = criteria.getRestrictions().stream()
                .filter(r -> r.getType().equals(Restriction.RestrictionType.IN))
                .map(r-> ((InRestriction)r).getValues())
                .flatMap(Collection::stream)
                .map(o-> (Integer)o)
                .collect(Collectors.toList());

        // remove id restriction since we don't want to see the application itself but its outages
        Collection<Restriction> restrictionsWithoutIdFilter = criteria.getRestrictions().stream()
                .filter(r -> !r.getType().equals(Restriction.RestrictionType.IN))
                .collect(Collectors.toList());
        criteria.setRestrictions(restrictionsWithoutIdFilter);

        // show only unresolved outages
        criteria.addRestriction(Restrictions.isNull("ifRegainedService"));

        // show only outages detected by a perspective poller
        criteria.addRestriction(Restrictions.isNotNull("perspective"));

        // find all relevant services by
        Set<OnmsMonitoredService> services = new HashSet<>();

        // a.) services of selected applications
        List<OnmsApplication> applications;
        if(selectedIds.isEmpty()) {
            applications = Collections.emptyList();
        } else {
            Criteria appCriteria = new CriteriaBuilder(OnmsApplication.class).in("id", selectedIds).toCriteria();
            applications = this.applicationDao.findMatching(appCriteria);
        }
        for(OnmsApplication application : applications) {
            services.addAll(application.getMonitoredServices());
        }

        // b.) directly selected interfaces
        for(int serviceId : selectedIds) {
            OnmsMonitoredService service = new OnmsMonitoredService();
            service.setId(serviceId);
            services.add(service);
        }

        if(services.isEmpty()) {
            criteria.addRestriction(Restrictions.sql("1=2")); // we don't want to find anything
        } else {
            criteria.addRestriction(Restrictions.in("monitoredService", services));
        }

        // set aliases so that the columns can be found
        criteria.setAliases(Arrays.asList(
                new Alias("monitoredService.ipInterface.node", "nodeLabel", Alias.JoinType.LEFT_JOIN),
                new Alias("monitoredService.ipInterface", "ipAddress", Alias.JoinType.LEFT_JOIN),
                new Alias("monitoredService.serviceType", "serviceName", Alias.JoinType.LEFT_JOIN),
                new Alias("perspective", "perspective", Alias.JoinType.LEFT_JOIN)));
    }
}
