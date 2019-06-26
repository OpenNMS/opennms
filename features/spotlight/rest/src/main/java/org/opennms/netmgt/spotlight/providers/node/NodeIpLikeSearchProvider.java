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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opennms.core.utils.IPLike;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.spotlight.api.Match;
import org.opennms.netmgt.spotlight.api.SearchProvider;
import org.opennms.netmgt.spotlight.api.SearchResult;
import org.opennms.netmgt.spotlight.providers.SearchResultBuilder;

public class NodeIpLikeSearchProvider implements SearchProvider {

    private final GenericPersistenceAccessor genericPersistenceAccessor;

    public NodeIpLikeSearchProvider(final GenericPersistenceAccessor genericPersistenceAccessor) {
        this.genericPersistenceAccessor = Objects.requireNonNull(genericPersistenceAccessor);
    }

    @Override
    public List<SearchResult> query(String input) {
        final List<SearchResult> searchResults = new ArrayList<>();
        if (isValidInput(input)) {
            final Map<String, Object> parameterMap = new HashMap<>();
            parameterMap.put("pattern", input);
            parameterMap.put("limit", 10);

            final List<Integer> nodeIds = genericPersistenceAccessor.executeNativeQuery(
                    "select node.nodeid from node join ipinterface on node.nodeid = ipinterface.nodeid where iplike(ipinterface.ipaddr, :pattern) order by node.nodelabel ASC limit :limit",
                    parameterMap);
            if (!nodeIds.isEmpty()) {
                final List<OnmsNode> matchingNodes = genericPersistenceAccessor.find("Select n from OnmsNode n where n.id in (?)", nodeIds.toArray());
                matchingNodes.stream().map(node -> {
                    final SearchResult searchResult = new SearchResultBuilder().withOnmsNode(node).build();
                    node.getIpInterfaces().stream()
                            .filter(ipInterface -> IPLike.matches(ipInterface.getIpAddress(), input))
                            .forEach(ipInterface -> searchResult.addMatch(new Match("ipInterface.ipAddress", "IP Address", InetAddressUtils.str(ipInterface.getIpAddress()))));
                    return searchResult;
                })
                .forEach(searchResults::add);
            }
        }
        return searchResults;
    }

    private boolean isValidInput(String input) {
        try {
            IPLike.matches("127.0.0.1", input);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
