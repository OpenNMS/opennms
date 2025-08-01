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
