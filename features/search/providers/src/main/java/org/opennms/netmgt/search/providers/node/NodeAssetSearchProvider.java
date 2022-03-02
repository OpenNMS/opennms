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

package org.opennms.netmgt.search.providers.node;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.core.rpc.utils.mate.EntityScopeProvider;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.search.api.Contexts;
import org.opennms.netmgt.search.api.Matcher;
import org.opennms.netmgt.search.api.SearchContext;
import org.opennms.netmgt.search.api.SearchProvider;
import org.opennms.netmgt.search.api.SearchQuery;
import org.opennms.netmgt.search.api.SearchResult;
import org.opennms.netmgt.search.api.SearchResultItem;
import org.opennms.netmgt.search.api.QueryUtils;
import org.opennms.netmgt.search.providers.SearchResultItemBuilder;

import com.google.common.collect.Lists;

public class NodeAssetSearchProvider implements SearchProvider {

    private final NodeDao nodeDao;
    private final EntityScopeProvider entityScopeProvider;

    public NodeAssetSearchProvider(final NodeDao nodeDao, final EntityScopeProvider entityScopeProvider) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.entityScopeProvider = Objects.requireNonNull(entityScopeProvider);
    }

    @Override
    public SearchContext getContext() {
        return Contexts.Node;
    }

    @Override
    public SearchResult query(SearchQuery query) {
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
                        Restrictions.ilike("assetRecord.storagectrl", QueryUtils.ilike(input))
                    )
            )
            .distinct();
        final int totalCount = nodeDao.countMatching(criteriaBuilder.toCriteria());
        final List<OnmsNode> matchingNodes = nodeDao.findMatching(criteriaBuilder.orderBy("label").limit(query.getMaxResults()).toCriteria());
        final List<SearchResultItem> results = matchingNodes.stream()
            .map(node -> {
                final SearchResultItem result = new SearchResultItemBuilder().withOnmsNode(node, entityScopeProvider).build();
                final OnmsAssetRecord record = node.getAssetRecord();
                final List<Matcher> matcherList = Lists.newArrayList(
                        new Matcher("Category", record.getCategory()),
                        new Matcher("ManuFacturer", record.getManufacturer()),
                        new Matcher("Vendor", record.getVendor()),
                        new Matcher("Model Number", record.getModelNumber()),
                        new Matcher("Serial Number", record.getSerialNumber()),
                        new Matcher("Description", record.getDescription()),
                        new Matcher("Circuit Id", record.getCircuitId()),
                        new Matcher("Asset Number", record.getAssetNumber()),
                        new Matcher("OS", record.getOperatingSystem()),
                        new Matcher("Rack", record.getRack()),
                        new Matcher("Slot", record.getSlot()),
                        new Matcher("Port", record.getPort()),
                        new Matcher("Region", record.getRegion()),
                        new Matcher("Division", record.getDivision()),
                        new Matcher("Department", record.getDepartment()),
                        new Matcher("Building", record.getBuilding()),
                        new Matcher("Floor", record.getFloor()),
                        new Matcher("Room", record.getRoom()),
                        new Matcher("Vendor Phone", record.getVendorPhone()),
                        new Matcher("Vendor Fax", record.getVendorFax()),
                        new Matcher("Vendor Asset Number", record.getVendorAssetNumber()),
                        new Matcher("Username", record.getUsername()),
                        new Matcher("Connection", record.getConnection()),
                        new Matcher("Lease", record.getLease()),
                        new Matcher("Lease Expires", record.getLeaseExpires()),
                        new Matcher("Support Phone", record.getSupportPhone()),
                        new Matcher("Maint. Contract Expiration", record.getMaintContractExpiration()),
                        new Matcher("Display Category", record.getDisplayCategory()),
                        new Matcher("Poller Category", record.getPollerCategory()),
                        new Matcher("Threshold Category", record.getThresholdCategory()),
                        new Matcher("Comment", record.getComment()),
                        new Matcher("CPU", record.getCpu()),
                        new Matcher("Ram", record.getRam()),
                        new Matcher("HDD1", record.getHdd1()),
                        new Matcher("HDD2", record.getHdd2()),
                        new Matcher("HDD3", record.getHdd3()),
                        new Matcher("HDD4", record.getHdd4()),
                        new Matcher("HDD5", record.getHdd5()),
                        new Matcher("HDD6", record.getHdd6()),
                        new Matcher("# Power Supplies", record.getNumpowersupplies()),
                        new Matcher("Input Power", record.getInputpower()),
                        new Matcher("Additional Hardware", record.getAdditionalhardware()),
                        new Matcher("admin", record.getAdmin()),
                        new Matcher("SNMP Community", record.getSnmpcommunity()),
                        new Matcher("RU Height", record.getRackunitheight()),
                        new Matcher("Managed Object Type", record.getManagedObjectType()),
                        new Matcher("Managed Object Instance", record.getManagedObjectInstance()),
                        new Matcher("Storage Controller", record.getStoragectrl())
                );
                result.addMatches(matcherList, input);
                return result;
            })
            .collect(Collectors.toList());
        final SearchResult searchResult = new SearchResult(Contexts.Node).withMore(totalCount > results.size()).withResults(results);
        return searchResult;
    }
}
