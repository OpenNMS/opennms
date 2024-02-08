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
