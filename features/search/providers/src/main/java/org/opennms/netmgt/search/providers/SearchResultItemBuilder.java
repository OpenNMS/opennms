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

package org.opennms.netmgt.search.providers;

import java.util.Objects;

import org.opennms.core.rpc.utils.mate.EntityScopeProvider;
import org.opennms.core.rpc.utils.mate.EntityScopeProviderImpl;
import org.opennms.core.rpc.utils.mate.Interpolator;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.search.api.Match;
import org.opennms.netmgt.search.api.SearchResultItem;
import org.opennms.netmgt.search.providers.node.NodeRef;

import com.google.common.collect.ImmutableMap;

public class SearchResultItemBuilder {

    private final String CUSTOM_INFO = System.getProperty("org.opennms.netmgt.search.info", "");

    private final SearchResultItem searchResultItem = new SearchResultItem();

    public SearchResultItemBuilder withOnmsNode(final OnmsNode node, final EntityScopeProvider entityScopeProvider) {
        Objects.requireNonNull(node);

        final NodeRef nodeRef = new NodeRef(node);
        searchResultItem.setIdentifier(nodeRef.asString());
        searchResultItem.setUrl("element/node.jsp?node=" + node.getId());
        searchResultItem.setLabel(node.getLabel());
        searchResultItem.setInfo(Interpolator.interpolate(CUSTOM_INFO, entityScopeProvider.getScopeForNode(node.getId())).output);
        searchResultItem.setProperties(ImmutableMap.<String, String>builder()
                .put("label", node.getLabel())
                .put("foreignId", node.getForeignId())
                .put("foreignSource", node.getForeignSource()).build());

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
