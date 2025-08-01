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
package org.opennms.netmgt.dao.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.newts.support.NewtsUtils;
import org.opennms.netmgt.newts.support.SearchableResourceMetadataCache;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.search.BooleanQuery;
import org.opennms.newts.api.search.Query;
import org.opennms.newts.api.search.SearchResults;
import org.opennms.newts.api.search.TermQuery;
import org.opennms.newts.cassandra.search.CassandraSearcher;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Used to verify the {@link org.opennms.netmgt.dao.support.ResourceStorageDao} interface implemented
 * by the {@link org.opennms.netmgt.dao.support.NewtsResourceStorageDao}.
 *
 * Resources are indexed using {@link NewtsUtils#addIndicesToAttributes} and primitive searching is
 * performed using a mock {@link org.opennms.newts.cassandra.search.CassandraSearcher}.
 *
 * @author jwhite
 */
public class NewtsResourceStorageDaoTest {

    private SearchableResourceMetadataCache m_cache = new MockSearchableResourceMetadataCache();
    private Context m_context = Context.DEFAULT_CONTEXT;
    private NewtsResourceStorageDao m_nrs;
    private CassandraSearcher m_searcher;
    private Map<ResourcePath, Set<String>> m_indexedPaths = Maps.newHashMap();

    @Before
    public void setUp() {
        m_searcher = mock(CassandraSearcher.class);

        m_nrs = new NewtsResourceStorageDao();
        m_nrs.setSearcher(m_searcher);
        m_nrs.setSearchableCache(m_cache);
        m_nrs.setContext(Context.DEFAULT_CONTEXT);
    }

    @Test
    public void exists() {
        // Path is missing when the resource does not exist
        replay();
        assertFalse(m_nrs.exists(ResourcePath.get("should", "not", "exist"), 1));
        doVerify();

        // Path is missing when the resource exists, but does not have a bucket
        index(ResourcePath.get("a"));
        replay();
        assertFalse(m_nrs.exists(ResourcePath.get("a"), 1));
        doVerify();

        // Path exists when a child resource with a bucket exists
        index(ResourcePath.get("a", "b", "bucket"));
        replay();
        assertTrue(m_nrs.exists(ResourcePath.get("a"), 1));
        doVerify();

        // Path exists when the resource with a bucket exists
        index(ResourcePath.get("a", "bucket"));
        replay();
        assertTrue(m_nrs.exists(ResourcePath.get("a"), 0));
        doVerify();
    }

    @Test
    public void existsWithin() {
        index(ResourcePath.get("a", "b", "c", "bucket"));
        replay();
        assertTrue(m_nrs.exists(ResourcePath.get("a", "b", "c"), 0));
        assertTrue(m_nrs.existsWithin(ResourcePath.get("a", "b", "c"), 0));
        assertTrue(m_nrs.existsWithin(ResourcePath.get("a", "b", "c"), 1));
        assertTrue(m_nrs.existsWithin(ResourcePath.get("a", "b"), 1));
        assertFalse(m_nrs.existsWithin(ResourcePath.get("a", "b"), 0));
        doVerify();
    }

    @Test
    public void children() {
        // Children are empty when the resource id does not exist
        replay();
        assertEquals(0, m_nrs.children(ResourcePath.get("should", "not", "exist"), 1).size());
        doVerify();

        // Children are empty when there are no child resources
        index(ResourcePath.get("a"));
        replay();
        assertEquals(0, m_nrs.children(ResourcePath.get("a"), 1).size());
        doVerify();

        // Child exists when the is a child resource
        index(ResourcePath.get("a", "b", "bucket"));
        replay();
        Set<ResourcePath> children = m_nrs.children(ResourcePath.get("a"), 1);
        assertEquals(1, children.size());
        assertEquals(ResourcePath.get("a", "b"), children.iterator().next());
        doVerify();

        // Same call but specifying the depth
        index(ResourcePath.get("a", "b", "bucket"));
        replay();
        children = m_nrs.children(ResourcePath.get("a"), 1);
        assertEquals(1, children.size());
        assertEquals(ResourcePath.get("a", "b"), children.iterator().next());
        doVerify();

        // Only returns the next level
        index(ResourcePath.get("a", "b", "c", "bucket"));
        replay();
        children = m_nrs.children(ResourcePath.get("a"), 2);
        assertEquals(1, children.size());
        assertEquals(ResourcePath.get("a", "b"), children.iterator().next());
        doVerify();

        // No children when depth is 0
        index(ResourcePath.get("a", "b", "bucket"));
        replay();
        children = m_nrs.children(ResourcePath.get("a"), 0);
        assertEquals(0, children.size());
        doVerify();
    }

    @Test
    public void getAttributes() {
        // Attributes are empty when the resource does not exist
        replay();
        assertEquals(0, m_nrs.getAttributes(ResourcePath.get("should", "not", "exist")).size());
        doVerify();

        // Metrics from all buckets should be present
        index(ResourcePath.get("a", "bucket1"), Sets.newHashSet("metric11", "metric12"));
        index(ResourcePath.get("a", "bucket2"), Sets.newHashSet("metric21", "metric22"));
        replay();
        Set<OnmsAttribute> attributes = m_nrs.getAttributes(ResourcePath.get("a"));
        assertEquals(4, attributes.size());

        // Verify the properties of a specific attribute
        RrdGraphAttribute metric11 = null;
        for (OnmsAttribute attribute : attributes) {
            if (attribute instanceof RrdGraphAttribute) {
                RrdGraphAttribute graphAttribute = (RrdGraphAttribute)attribute;
                if ("metric11".equals(graphAttribute.getName())) {
                    metric11 = graphAttribute;
                }
            }
        }
        assertNotNull(metric11);

        doVerify();
    }

    @Test
    public void getResponseTimeAttributes() {
        index(ResourcePath.get(ResourceTypeUtils.RESPONSE_DIRECTORY, "127.0.0.1", "strafeping"), Sets.newHashSet("ping1", "ping2"));
        replay();
        Set<OnmsAttribute> attributes = m_nrs.getAttributes(ResourcePath.get(ResourceTypeUtils.RESPONSE_DIRECTORY, "127.0.0.1"));

        // Response time metrics should be treated as a single attribute
        assertEquals(1, attributes.size());
        assertEquals("strafeping", attributes.iterator().next().getName());

        doVerify();
    }

    private void index(ResourcePath path) {
        index(path, Sets.newHashSet());
    }

    private void index(ResourcePath path, Set<String> metrics) {
        m_indexedPaths.put(path, metrics);
    }

    private void replay() {
        doAnswer(new Answer<SearchResults>() {
            @Override
            public SearchResults answer(final InvocationOnMock invocation) throws Throwable {
                // Assume there is a single term query
                Query q = invocation.getArgument(1);
                BooleanQuery bq = (BooleanQuery)q;
                TermQuery tq = (TermQuery)bq.getClauses().get(0).getQuery();
                String field = tq.getTerm().getField("");
                String value = tq.getTerm().getValue();

                SearchResults searchResults = new SearchResults();
                for (Entry<ResourcePath, Set<String>> entry : m_indexedPaths.entrySet()) {
                    Map<String, String> attributes = Maps.newHashMap();
                    // Build the indexed attributes and attempt to match them against the given query
                    NewtsUtils.addIndicesToAttributes(entry.getKey(), attributes);
                    if (value.equals(attributes.get(field))) {
                        searchResults.addResult(new Resource(NewtsUtils.toResourceId(entry.getKey())), entry.getValue());
                    }
                }
                return searchResults;
            }
        }).when(m_searcher).search(eq(m_context), any(), anyBoolean());
        when(m_searcher.getResourceAttributes(eq(m_context), any())).thenReturn(Maps.newHashMap());
    }

    private void doVerify() {
        verify(m_searcher, atLeastOnce()).search(eq(m_context), any(Query.class), anyBoolean());
        verify(m_searcher, atLeast(0)).getResourceAttributes(eq(m_context), anyString());
        verifyNoMoreInteractions(m_searcher);
        m_indexedPaths.clear();
    }
}
