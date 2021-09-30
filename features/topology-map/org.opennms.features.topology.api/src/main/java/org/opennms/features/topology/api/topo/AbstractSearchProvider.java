/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.topo;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;

/**
 * This abstract class provides empty implementations of all of the event handler functions.
 * @author root
 *
 */
public abstract class AbstractSearchProvider implements SearchProvider {

    public static final boolean supportsPrefix(String providerPrefix, String searchPrefix) {
        if (searchPrefix == null || "".equals(searchPrefix)) {
            return false;
        }
        return providerPrefix.startsWith(searchPrefix.substring(0, Math.min(searchPrefix.length(), providerPrefix.length())).toLowerCase());
    }

    @Override
    public void onFocusSearchResult(SearchResult searchResult, OperationContext operationContext) {
    }

    @Override
    public void onDefocusSearchResult(SearchResult searchResult, OperationContext operationContext) {
    }

    @Override
    public void onCenterSearchResult(SearchResult searchResult, GraphContainer graphContainer) {
    }

    @Override
    public void onToggleCollapse(SearchResult searchResult, GraphContainer graphContainer) {
    }
}
