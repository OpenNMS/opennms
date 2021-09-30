/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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
