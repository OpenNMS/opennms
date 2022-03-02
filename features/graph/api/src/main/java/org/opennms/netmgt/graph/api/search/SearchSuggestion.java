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

import java.util.Comparator;
import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * A {@link SearchSuggestion} is provided to the user and was created from a (partial) search query.
 * A suggestion is a very abstract and may not directly relate to a vertex. For example a category.
 * The main idea is, that a {@link SearchSuggestion} represents an item a user can select, which
 * afterwards is resolved to a List of vertices/edges.
 */
public class SearchSuggestion implements Comparable<SearchSuggestion> {

    // The context of the suggestion, e.g. category, attribute, node, etc. to allow for a more fine grain suggestion
    // This may be achieved later by sub-classing
    private String context;

    // A unique identifier of the "thing" represented by the suggestion.
    // This may be used by the provider to determine which "thing" in OpenNMS the suggestion represents
    // (e.g. category id, node id, etc.)
    private String id;

    // The user-friendly label for the suggestion, which is shown to the user
    private String label;

    // The provider from which the suggestion is. This is required to resolve later on
    // This ensures that the originating SearchProvider can actually resolve it
    private String provider;

    public SearchSuggestion(String providerId, String context, String id, String label) {
        setId(Objects.requireNonNull(id));
        setLabel(Objects.requireNonNull(label));
        setContext(Objects.requireNonNull(context));
        setProvider(Objects.requireNonNull(providerId));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchSuggestion that = (SearchSuggestion) o;
        return Objects.equals(context, that.context)
                && Objects.equals(id, that.id)
                && Objects.equals(label, that.label)
                && Objects.equals(provider, that.provider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, id, label, provider);
    }

    @Override
    public int compareTo(SearchSuggestion that) {
        return Objects.compare(this, that,
                Comparator.comparing(SearchSuggestion::getLabel)
                        .thenComparing(SearchSuggestion::getProvider)
                        .thenComparing(SearchSuggestion::getContext)
        );
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("context", context)
                .add("id", id)
                .add("label", label)
                .add("provider", provider)
                .toString();
    }
}
