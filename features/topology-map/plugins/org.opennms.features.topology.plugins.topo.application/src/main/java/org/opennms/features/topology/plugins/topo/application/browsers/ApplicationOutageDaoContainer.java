/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
