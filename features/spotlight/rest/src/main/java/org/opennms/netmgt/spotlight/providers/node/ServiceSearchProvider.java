/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.spotlight.providers.node;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.spotlight.api.SearchProvider;
import org.opennms.netmgt.spotlight.api.SearchResult;

public class ServiceSearchProvider implements SearchProvider {

    private final ServiceTypeDao serviceTypeDao;

    public ServiceSearchProvider(final ServiceTypeDao serviceTypeDao) {
        this.serviceTypeDao = Objects.requireNonNull(serviceTypeDao);
    }

    // TODO MVR this is currently  an action search but it actually should show nodes with a service match
    @Override
    public List<SearchResult> query(String input) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsServiceType.class)
                .ilike("name", input)
                .distinct()
                .limit(10); // TODO MVR make configurable
        final Criteria criteria = builder.toCriteria();
        final List<OnmsServiceType> matchingResult = serviceTypeDao.findMatching(criteria);
        final List<SearchResult> searchResults = matchingResult.stream().map(service -> {
            final SearchResult searchResult = new SearchResult();
            searchResult.setContext("Providing Service");
            searchResult.setIdentifer(service.getId().toString());
            searchResult.setLabel("Show nodes with service '" + service.getName() + "'");
            searchResult.setUrl("element/nodeList.htm?service=" + service.getId());
            return searchResult;
        }).collect(Collectors.toList());
        return searchResults;
    }
}
