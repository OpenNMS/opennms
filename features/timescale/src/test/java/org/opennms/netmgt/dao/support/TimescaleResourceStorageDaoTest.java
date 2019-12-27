/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.timescale.support.SearchableResourceMetadataCache;
import org.opennms.netmgt.timescale.support.TimescaleUtils;
import org.opennms.netmgt.timeseries.api.domain.StorageException;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.search.BooleanQuery;
import org.opennms.newts.api.search.Query;
import org.opennms.newts.api.search.SearchResults;
import org.opennms.newts.api.search.TermQuery;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Used to verify the {@link org.opennms.netmgt.dao.support.TimescaleResourceStorageDao} interface implemented
 * by the {@link org.opennms.netmgt.dao.support.TimescaleResourceStorageDao}.
 *
 * Resources are indexed using {@link TimescaleUtils#addIndicesToAttributes} and primitive searching is
 * performed using a mock {@link org.opennms.newts.cassandra.search.CassandraSearcher}.
 *
 * @author jwhite
 */
public class TimescaleResourceStorageDaoTest {

    private SearchableResourceMetadataCache m_cache = new MockSearchableResourceMetadataCache2();
    private Context m_context = Context.DEFAULT_CONTEXT;
    private TimescaleResourceStorageDao resourceStorageDao;
    private TimescaleSearcher searcher;
    private Map<ResourcePath, Set<String>> indexedPaths = Maps.newHashMap();

    @Before
    public void setUp() {
        searcher = EasyMock.createNiceMock(TimescaleSearcher.class);

        resourceStorageDao = new TimescaleResourceStorageDao();
        resourceStorageDao.setSearcher(searcher);
        resourceStorageDao.setSearchableCache(m_cache);
        resourceStorageDao.setContext(Context.DEFAULT_CONTEXT);
    }

    @Test
    public void exists() throws StorageException {
        // Path is missing when the resource does not exist
        replay();
        assertFalse(resourceStorageDao.exists(ResourcePath.get("should", "not", "exist"), 1));
        verify();

        // Path is missing when the resource exists, but does not have a bucket
        index(ResourcePath.get("a"));
        replay();
        assertFalse(resourceStorageDao.exists(ResourcePath.get("a"), 1));
        verify();

        // Path exists when a child resource with a bucket exists
        index(ResourcePath.get("a", "b", "bucket"));
        replay();
        assertTrue(resourceStorageDao.exists(ResourcePath.get("a"), 1));
        verify();

        // Path exists when the resource with a bucket exists
        index(ResourcePath.get("a", "bucket"));
        replay();
        assertTrue(resourceStorageDao.exists(ResourcePath.get("a"), 0));
        verify();
    }

    @Test
    public void existsWithin() throws StorageException {
        index(ResourcePath.get("a", "b", "c", "bucket"));
        replay();
        assertTrue(resourceStorageDao.exists(ResourcePath.get("a", "b", "c"), 0));
        assertTrue(resourceStorageDao.existsWithin(ResourcePath.get("a", "b", "c"), 0));
        assertTrue(resourceStorageDao.existsWithin(ResourcePath.get("a", "b", "c"), 1));
        assertTrue(resourceStorageDao.existsWithin(ResourcePath.get("a", "b"), 1));
        assertFalse(resourceStorageDao.existsWithin(ResourcePath.get("a", "b"), 0));
        verify();
    }

    @Test
    public void children() throws StorageException {
        // Children are empty when the resource id does not exist
        replay();
        assertEquals(0, resourceStorageDao.children(ResourcePath.get("should", "not", "exist"), 1).size());
        verify();

        // Children are empty when there are no child resources
        index(ResourcePath.get("a"));
        replay();
        assertEquals(0, resourceStorageDao.children(ResourcePath.get("a"), 1).size());
        verify();

        // Child exists when the is a child resource
        index(ResourcePath.get("a", "b", "bucket"));
        replay();
        Set<ResourcePath> children = resourceStorageDao.children(ResourcePath.get("a"), 1);
        assertEquals(1, children.size());
        assertEquals(ResourcePath.get("a", "b"), children.iterator().next());
        verify();

        // Same call but specifying the depth
        index(ResourcePath.get("a", "b", "bucket"));
        replay();
        children = resourceStorageDao.children(ResourcePath.get("a"), 1);
        assertEquals(1, children.size());
        assertEquals(ResourcePath.get("a", "b"), children.iterator().next());
        verify();

        // Only returns the next level
        index(ResourcePath.get("a", "b", "c", "bucket"));
        replay();
        children = resourceStorageDao.children(ResourcePath.get("a"), 2);
        assertEquals(1, children.size());
        assertEquals(ResourcePath.get("a", "b"), children.iterator().next());
        verify();

        // No children when depth is 0
        index(ResourcePath.get("a", "b", "bucket"));
        replay();
        children = resourceStorageDao.children(ResourcePath.get("a"), 0);
        assertEquals(0, children.size());
        verify();
    }

    @Test
    public void getAttributes() throws StorageException {
        // Attributes are empty when the resource does not exist
        replay();
        assertEquals(0, resourceStorageDao.getAttributes(ResourcePath.get("should", "not", "exist")).size());
        verify();

        // Metrics from all buckets should be present
        index(ResourcePath.get("a", "bucket1"), Sets.newHashSet("metric11", "metric12"));
        index(ResourcePath.get("a", "bucket2"), Sets.newHashSet("metric21", "metric22"));
        replay();
        Set<OnmsAttribute> attributes = resourceStorageDao.getAttributes(ResourcePath.get("a"));
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

        verify();
    }

    @Test
    public void getResponseTimeAttributes() throws StorageException {
        index(ResourcePath.get(ResourceTypeUtils.RESPONSE_DIRECTORY, "127.0.0.1", "strafeping"), Sets.newHashSet("ping1", "ping2"));
        replay();
        Set<OnmsAttribute> attributes = resourceStorageDao.getAttributes(ResourcePath.get(ResourceTypeUtils.RESPONSE_DIRECTORY, "127.0.0.1"));

        // Response time metrics should be treated as a single attribute
        assertEquals(1, attributes.size());
        assertEquals("strafeping", attributes.iterator().next().getName());

        verify();
    }

    private void index(ResourcePath path) {
        index(path, Sets.newHashSet());
    }

    private void index(ResourcePath path, Set<String> metrics) {
        indexedPaths.put(path, metrics);
    }

    private void replay() throws StorageException {
        EasyMock.expect(searcher.search(EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyBoolean())).andAnswer(new IAnswer<org.opennms.netmgt.dao.support.SearchResults>() {
            public org.opennms.netmgt.dao.support.SearchResults
            answer() throws Throwable {
                // Assume there is a single term query
                Query q = (Query)EasyMock.getCurrentArguments()[1];
                BooleanQuery bq = (BooleanQuery)q;
                TermQuery tq = (TermQuery)bq.getClauses().get(0).getQuery();
                String field = tq.getTerm().getField("");
                String value = tq.getTerm().getValue();

                org.opennms.netmgt.dao.support.SearchResults searchResults = new org.opennms.netmgt.dao.support.SearchResults();
                for (Entry<ResourcePath, Set<String>> entry : indexedPaths.entrySet()) {
                    Map<String, String> attributes = Maps.newHashMap();
                    // Build the indexed attributes and attempt to match them against the given query
                    TimescaleUtils.addIndicesToAttributes(entry.getKey(), attributes);
                    if (value.equals(attributes.get(field))) {
                        searchResults.addResult(new Resource(TimescaleUtils.toResourceId(entry.getKey())), entry.getValue());
                    }
                }
                return searchResults;
            }
        }).atLeastOnce();
        EasyMock.expect(searcher.getResourceAttributes(EasyMock.eq(m_context), EasyMock.anyObject())).andReturn(Maps.newHashMap()).anyTimes();
        EasyMock.replay(searcher);
    }

    private void verify() {
        EasyMock.verify(searcher);
        EasyMock.reset(searcher);
        indexedPaths.clear();
    }
}
