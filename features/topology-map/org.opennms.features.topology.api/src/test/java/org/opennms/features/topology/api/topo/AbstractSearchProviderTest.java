/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.opennms.features.topology.api.GraphContainer;

public class AbstractSearchProviderTest {

    public class TestVertexRef implements VertexRef{

        private final String m_id;
        private final String m_label;

        public TestVertexRef(String id, String label){
            m_id = id;
            m_label = label;
        }

        @Override
        public String getId() {
            return m_id;
        }

        @Override
        public String getNamespace() {
            return "nodes";
        }

        @Override
        public String getLabel() {
            return m_label;
        }

        @Override
        public int compareTo(Ref o) {
            return 0;
        }
    }

    public class ContainsMatcher extends AbstractSearchQuery {

        public ContainsMatcher(String queryString) {
            super(queryString);
        }

        @Override
        public boolean matches(String provided) {
            return provided.toLowerCase().contains(getQueryString().toLowerCase());
        }
    }

    public class ExactMatcher extends AbstractSearchQuery {

        public ExactMatcher(String queryString) {
            super(queryString);
        }

        @Override
        public boolean matches(String provided) {
            return provided.toLowerCase().matches(getQueryString().toLowerCase());
        }
    }

    @Test
    public void testSearchProvider(){
        SearchQuery containsQuery = new ContainsMatcher("node");
        SearchQuery exactQuery = new ExactMatcher("node-label-1");

        SearchProvider searchProvider1 = createSearchProvider();

        assertEquals(10, searchProvider1.query(containsQuery, null).size());
        assertEquals(1, searchProvider1.query(exactQuery, null).size());
    }

    private SearchProvider createSearchProvider() {
        return new AbstractSearchProvider() {

            List<VertexRef> m_vertexRefs = getVertexRefs();

            @Override
            public String getSearchProviderNamespace() {
                return "test-namespace";
            }

            @Override
            public boolean contributesTo(String namespace) {
                return false;
            }

            @Override
            public List<SearchResult> query(SearchQuery searchQuery, GraphContainer graphContainer) {
                List<SearchResult> verts = new ArrayList<>();
                for (VertexRef vertexRef : m_vertexRefs) {
                    if (searchQuery.matches(vertexRef.getLabel())) {
                        verts.add(new SearchResult(vertexRef.getNamespace(), vertexRef.getId(), vertexRef.getLabel(),
                                searchQuery.getQueryString(), !SearchResult.COLLAPSIBLE, !SearchResult.COLLAPSED));
                    }
                }
                return verts;
            }

            @Override
            public boolean supportsPrefix(String searchPrefix) {
                return false;
            }

            @Override
            public Set<VertexRef> getVertexRefsBy(SearchResult searchResult, GraphContainer container) {
                return Collections.emptySet();
            }

            @Override
            public void addVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
            }

            @Override
            public void removeVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
            }
        };
    }
    
    @Test
    public void testSupportsPrefix() {
        assertFalse(AbstractSearchProvider.supportsPrefix("category=", null));
        assertFalse(AbstractSearchProvider.supportsPrefix("category=", ""));
        assertFalse(AbstractSearchProvider.supportsPrefix("category=", "d"));
        assertTrue(AbstractSearchProvider.supportsPrefix("category=", "c"));
        assertTrue(AbstractSearchProvider.supportsPrefix("category=", "cat"));
        assertTrue(AbstractSearchProvider.supportsPrefix("category=", "category"));
        assertFalse(AbstractSearchProvider.supportsPrefix("category=", "categoryy"));
        assertTrue(AbstractSearchProvider.supportsPrefix("category=", "category="));
    }

    private List<VertexRef> getVertexRefs(){
        List<VertexRef> vertexRefs = new ArrayList<>();

        for(int i = 0; i < 10; i++) {
            vertexRefs.add(new TestVertexRef("" + i, "node-label-" + i));
        }

        return vertexRefs;
    }

}
