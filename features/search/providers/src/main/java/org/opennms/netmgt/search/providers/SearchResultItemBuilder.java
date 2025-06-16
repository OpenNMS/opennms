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
package org.opennms.netmgt.search.providers;

import java.util.Objects;

import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.search.api.Match;
import org.opennms.netmgt.search.api.SearchResultItem;

import com.google.common.collect.ImmutableMap;

public class SearchResultItemBuilder {

    private final String CUSTOM_INFO = System.getProperty("org.opennms.netmgt.search.info", "");

    private final SearchResultItem searchResultItem = new SearchResultItem();

    public SearchResultItemBuilder withOnmsNode(final OnmsNode node, final EntityScopeProvider entityScopeProvider) {
        Objects.requireNonNull(node);

        searchResultItem.setIdentifier(node.getNodeId());
        searchResultItem.setUrl("element/node.jsp?node=" + node.getId());
        searchResultItem.setLabel(node.getLabel());
        searchResultItem.setInfo(Interpolator.interpolate(CUSTOM_INFO, entityScopeProvider.getScopeForNode(node.getId())).output);

        final var properties = ImmutableMap.<String, String>builder()
                .put("label", node.getLabel());
        if (node.getForeignId() != null) {
            properties.put("foreignId", node.getForeignId());
        }
        if(node.getForeignSource() != null) {
            properties.put("foreignSource", node.getForeignSource());
        }
        searchResultItem.setProperties(properties.build());

        return this;
    }

    public SearchResultItemBuilder withMatch(String id, String label, String value) {
        searchResultItem.addMatch(new Match(id, label, value));
        return this;
    }

    public SearchResultItemBuilder withWeight(int weight) {
        searchResultItem.setWeight(weight);
        return this;
    }

    public SearchResultItem build() {
        return searchResultItem;
    }

}
