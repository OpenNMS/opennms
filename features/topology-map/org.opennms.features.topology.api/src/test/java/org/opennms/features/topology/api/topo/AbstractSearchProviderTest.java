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
