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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.spotlight.api.Match;
import org.opennms.netmgt.spotlight.api.SearchProvider;
import org.opennms.netmgt.spotlight.api.SearchQuery;
import org.opennms.netmgt.spotlight.api.SearchResult;
import org.opennms.netmgt.spotlight.providers.QueryUtils;
import org.opennms.netmgt.spotlight.providers.SearchResultBuilder;

public class NodeAssetSearchProvider implements SearchProvider {

    private final NodeDao nodeDao;

    public NodeAssetSearchProvider(NodeDao nodeDao) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
    }

    @Override
    public List<SearchResult> query(SearchQuery query) {
        final String input = query.getInput();
        final CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsNode.class)
            .alias("assetRecord", "assetRecord")
            .and(
                Restrictions.isNotNull("assetRecord"),
                    Restrictions.or(
                        Restrictions.ilike("assetRecord.category", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.manufacturer", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.vendor", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.modelNumber", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.serialNumber", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.description", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.circuitId", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.assetNumber", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.operatingSystem", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.rack", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.slot", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.port", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.region", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.division", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.department", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.building", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.floor", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.room", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.vendorPhone", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.vendorFax", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.vendorAssetNumber", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.username", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.password", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.connection", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.lease", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.leaseExpires", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.supportPhone", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.maintContractExpiration", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.displayCategory", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.pollerCategory", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.thresholdCategory", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.comment", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.cpu", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.ram", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.hdd1", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.hdd2", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.hdd3", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.hdd4", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.hdd5", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.hdd6", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.numpowersupplies", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.inputpower", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.additionalhardware", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.admin", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.snmpcommunity", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.rackunitheight", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.managedObjectType", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.managedObjectInstance", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.vmwareManagedObjectId", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.vmwareManagedEntityType", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.vmwareManagementServer", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.vmwareState", QueryUtils.ilike(input)),
                        Restrictions.ilike("assetRecord.storagectrl", QueryUtils.ilike(input))
                    )
            )
            .distinct()
            .limit(query.getMaxResults());
        final Criteria criteria = criteriaBuilder.toCriteria();
        final List<OnmsNode> matchingNodes = nodeDao.findMatching(criteria);
        final List<SearchResult> results = matchingNodes.stream()
            .map(node -> {
                final SearchResult result = new SearchResultBuilder().withOnmsNode(node).build();
                final OnmsAssetRecord record = node.getAssetRecord();
                // TODO MVR this is ugly as hell ...
                for (Method method : OnmsAssetRecord.class.getMethods()) {
                    if (method.getName().startsWith("get")
                            && !method.getName().toLowerCase().contains("topology")
                            && method.getReturnType() == String.class
                            && method.getParameterCount() == 0) {
                        try {
                            Object returnedValue = method.invoke(record);
                            if (returnedValue != null && QueryUtils.matches(returnedValue.toString(), input)) {
                                result.addMatch(new Match(method.getName(), method.getName().replace("get", ""), returnedValue.toString()));
                            }
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace(); // TODO MVR ignore for now
                        }
                    }
                }
                return result;
            })
            .collect(Collectors.toList());
        return results;
    }
}
