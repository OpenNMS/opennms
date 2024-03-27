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

import org.opennms.netmgt.graph.api.service.GraphService;

public class SearchContext {

    private final int suggestionsLimit;
    private final GraphService graphService;

    private SearchContext(final int suggestionsLimit,
                          final GraphService graphService) {
        this.suggestionsLimit = suggestionsLimit;
        this.graphService = Objects.requireNonNull(graphService);
    }

    public int getSuggestionsLimit() {
        return suggestionsLimit;
    }

    public GraphService getGraphService(){
        return this.graphService;
    }

    public static SearchContextBuilder builder(){
        return new SearchContextBuilder();
    }

    public final static class SearchContextBuilder {
        private int suggestionsLimit = 15;
        private GraphService graphService;

        public SearchContextBuilder suggestionsLimit(int suggestionsLimit) {
            this.suggestionsLimit = suggestionsLimit;
            return this;
        }

        public SearchContextBuilder graphService(GraphService graphService) {
            this.graphService = graphService;
            return this;
        }

        public SearchContext build() {
            return new SearchContext(
                    this.suggestionsLimit,
                    this.graphService
            );
        }
    }
}
