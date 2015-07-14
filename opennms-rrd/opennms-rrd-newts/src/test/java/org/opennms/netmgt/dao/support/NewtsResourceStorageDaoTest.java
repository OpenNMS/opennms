package org.opennms.netmgt.dao.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Test;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.rrd.newts.support.SearchableResourceMetadataCache;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.search.SearchResults;
import org.opennms.newts.cassandra.search.CassandraSearcher;
import org.opennms.newts.cassandra.search.ResourceMetadata;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class NewtsResourceStorageDaoTest {

    private SearchableResourceMetadataCache m_cache = new SearchableResourceMetadataCache() {
        @Override
        public void merge(Context context, Resource resource,
                ResourceMetadata rMetadata) {
            // pass
        }

        @Override
        public Optional<ResourceMetadata> get(Context context,
                Resource resource) {
            return Optional.absent();
        }

        @Override
        public List<String> getEntriesWithPrefix(Context context,
                String resourceIdPrefix) {
            return Collections.emptyList();
        }
    };

    @Test
    public void exists() throws IOException {
        Context context = Context.DEFAULT_CONTEXT;
        CassandraSearcher searcher = EasyMock.createNiceMock(CassandraSearcher.class);
        NewtsResourceStorageDao nrs = new NewtsResourceStorageDao();
        nrs.setSearcher(searcher);
        nrs.setSearchableCache(m_cache);
        nrs.setContext(context);

        // Path is missing when the resource does not exist
        SearchResults searchResults = new SearchResults();
        EasyMock.expect(searcher.search(EasyMock.eq(context), EasyMock.anyObject())).andReturn(searchResults);
        EasyMock.replay(searcher);

        assertFalse(nrs.exists(ResourcePath.get("should", "not", "exist")));
        EasyMock.verify(searcher);
        EasyMock.reset(searcher);

        // Path is missing when the resource exists, but does not have a bucket
        searchResults = new SearchResults();
        searchResults.addResult(new Resource("a"), Collections.emptyList());
        EasyMock.expect(searcher.search(EasyMock.eq(context), EasyMock.anyObject())).andReturn(searchResults);
        EasyMock.replay(searcher);

        assertFalse(nrs.exists(ResourcePath.get("a")));
        EasyMock.verify(searcher);
        EasyMock.reset(searcher);

        // Path exists when a child resource with a bucket exists
        searchResults = new SearchResults();
        searchResults.addResult(new Resource("a:b:bucket"), Collections.emptyList());
        EasyMock.expect(searcher.search(EasyMock.eq(context), EasyMock.anyObject())).andReturn(searchResults);
        EasyMock.replay(searcher);

        assertTrue(nrs.exists(ResourcePath.get("a")));
        EasyMock.verify(searcher);
        EasyMock.reset(searcher);

        // Path exists when the resource with a bucket exists
        searchResults = new SearchResults();
        searchResults.addResult(new Resource("a:bucket"), Collections.emptyList());
        EasyMock.expect(searcher.search(EasyMock.eq(context), EasyMock.anyObject())).andReturn(searchResults);
        EasyMock.replay(searcher);

        assertTrue(nrs.exists(ResourcePath.get("a")));
        EasyMock.verify(searcher);
        EasyMock.reset(searcher);
    }

    @Test
    public void children() throws IOException {
        Context context = Context.DEFAULT_CONTEXT;
        CassandraSearcher searcher = EasyMock.createNiceMock(CassandraSearcher.class);
        NewtsResourceStorageDao nrs = new NewtsResourceStorageDao();
        nrs.setSearcher(searcher);
        nrs.setContext(context);

        // Children are empty when the resource id does not exist
        SearchResults searchResults = new SearchResults();
        EasyMock.expect(searcher.search(EasyMock.eq(context), EasyMock.anyObject())).andReturn(searchResults);
        EasyMock.replay(searcher);

        assertEquals(0, nrs.children(ResourcePath.get("should", "not", "exist")).size());
        EasyMock.verify(searcher);
        EasyMock.reset(searcher);

        // Children are empty when there are no child resources
        searchResults = new SearchResults();
        EasyMock.expect(searcher.search(EasyMock.eq(context), EasyMock.anyObject())).andReturn(searchResults);
        EasyMock.replay(searcher);

        assertEquals(0, nrs.children(ResourcePath.get("a")).size());
        EasyMock.verify(searcher);
        EasyMock.reset(searcher);

        // Child exists when the is a child resource
        searchResults = new SearchResults();
        searchResults.addResult(new Resource("a:b:bucket"), Collections.emptyList());
        EasyMock.expect(searcher.search(EasyMock.eq(context), EasyMock.anyObject())).andReturn(searchResults);
        EasyMock.replay(searcher);

        Set<ResourcePath> children = nrs.children(ResourcePath.get("a"));
        assertEquals(1, children.size());
        assertEquals(ResourcePath.get("a", "b"), children.iterator().next());
        EasyMock.verify(searcher);
        EasyMock.reset(searcher);

        // Same call but specifying the depth
        searchResults = new SearchResults();
        searchResults.addResult(new Resource("a:b:bucket"), Collections.emptyList());
        EasyMock.expect(searcher.search(EasyMock.eq(context), EasyMock.anyObject())).andReturn(searchResults);
        EasyMock.replay(searcher);

        children = nrs.children(ResourcePath.get("a"), 1);
        assertEquals(1, children.size());
        assertEquals(ResourcePath.get("a", "b"), children.iterator().next());
        EasyMock.verify(searcher);
        EasyMock.reset(searcher);

        // Only returns the next level
        searchResults = new SearchResults();
        searchResults.addResult(new Resource("a:b:c:bucket"), Collections.emptyList());
        EasyMock.expect(searcher.search(EasyMock.eq(context), EasyMock.anyObject())).andReturn(searchResults);
        EasyMock.replay(searcher);

        children = nrs.children(ResourcePath.get("a"));
        assertEquals(1, children.size());
        assertEquals(ResourcePath.get("a", "b"), children.iterator().next());
        EasyMock.verify(searcher);
        EasyMock.reset(searcher);
        
        // No children when depth is 0
        searchResults = new SearchResults();
        searchResults.addResult(new Resource("a:b:bucket"), Collections.emptyList());
        EasyMock.expect(searcher.search(EasyMock.eq(context), EasyMock.anyObject())).andReturn(searchResults);
        EasyMock.replay(searcher);

        children = nrs.children(ResourcePath.get("a"), 0);
        assertEquals(0, children.size());
        EasyMock.verify(searcher);
        EasyMock.reset(searcher);
    }

    @Test
    public void getAttributes() throws IOException {
        Context context = Context.DEFAULT_CONTEXT;
        CassandraSearcher searcher = EasyMock.createNiceMock(CassandraSearcher.class);
        NewtsResourceStorageDao nrs = new NewtsResourceStorageDao();
        nrs.setSearcher(searcher);
        nrs.setContext(context);

        // Attributes are empty when the resource does not exist
        SearchResults searchResults = new SearchResults();
        EasyMock.expect(searcher.search(EasyMock.eq(context), EasyMock.anyObject())).andReturn(searchResults);
        EasyMock.expect(searcher.getResourceAttributes(EasyMock.eq(context), EasyMock.anyObject())).andReturn(Maps.newHashMap());
        EasyMock.replay(searcher);

        assertEquals(0, nrs.getAttributes(ResourcePath.get("should", "not", "exist")).size());
        EasyMock.verify(searcher);
        EasyMock.reset(searcher);

        // Metrics from all buckets should be present
        searchResults = new SearchResults();
        searchResults.addResult(new Resource("a:bucket1"), Sets.newHashSet("metric11", "metric12"));
        searchResults.addResult(new Resource("a:bucket2"), Sets.newHashSet("metric21", "metric22"));
        EasyMock.expect(searcher.search(EasyMock.eq(context), EasyMock.anyObject())).andReturn(searchResults);
        EasyMock.expect(searcher.getResourceAttributes(EasyMock.eq(context), EasyMock.anyObject())).andReturn(Maps.newHashMap());
        EasyMock.replay(searcher);

        Set<OnmsAttribute> attributes = nrs.getAttributes(ResourcePath.get("a"));
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

        EasyMock.verify(searcher);
        EasyMock.reset(searcher);
    }
}
