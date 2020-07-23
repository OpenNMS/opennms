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

package org.opennms.netmgt.graph.api.search;

import java.util.Objects;

/**
 * After the user selected a Suggestion, this is converted to a {@link SearchCriteria}.
 * Theoretically they are identically, but differ only by name in order to distinguish the different use-cases.
 */
public class SearchCriteria {

    // The provider to use for search (e.g. from the SearchSuggestion)
    private String providerId;

    // The namespace of the graph, the search was triggered for
    private String namespace;

    // The search criteria, usually the id of the SearchSuggestion
    private String criteria;

    public SearchCriteria() {

    }

    public SearchCriteria(String providerId, String namespace, final String criteria) {
        this.providerId = Objects.requireNonNull(providerId);
        this.namespace = Objects.requireNonNull(namespace);
        this.criteria = Objects.requireNonNull(criteria);
    }

    public SearchCriteria(final SearchSuggestion suggestion, final String namespace) {
        this(suggestion.getProvider(), namespace, suggestion.getLabel());
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

}
