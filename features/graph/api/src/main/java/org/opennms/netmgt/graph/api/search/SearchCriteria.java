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
