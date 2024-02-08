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
package org.opennms.netmgt.timeseries.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.opennms.netmgt.timeseries.util.TimeseriesUtils.toResourceId;
import static org.opennms.netmgt.timeseries.util.TimeseriesUtils.toSearchRegex;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opennms.integration.api.v1.timeseries.IntrinsicTagNames;
import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.integration.api.v1.timeseries.StorageException;
import org.opennms.integration.api.v1.timeseries.Tag;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableMetric;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.model.RrdGraphAttribute;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.re2j.Pattern;

/**
 * Used to verify the {@link TimeseriesResourceStorageDao} interface implemented
 * by the {@link TimeseriesResourceStorageDao}.
 *
 *
 * @author jwhite
 */
public class TimeseriesResourceStorageDaoTest {

    private TimeseriesResourceStorageDao resourceStorageDao;
    private TimeseriesSearcher searcher;
    private Map<ResourcePath, Set<String>> indexedPaths = Maps.newHashMap();

    @Before
    public void setUp() {
        searcher = mock(TimeseriesSearcher.class);
        resourceStorageDao = new TimeseriesResourceStorageDao();
        resourceStorageDao.setSearcher(searcher);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(searcher);
        indexedPaths.clear();
    }

    @Test
    public void exists() throws StorageException {
        // Path is missing when the resource does not exist
        replay();
        assertFalse(resourceStorageDao.exists(ResourcePath.get("should", "not", "exist"), 1));
        

        // Path is missing when the resource exists, but does not have a bucket
        index(ResourcePath.get("a"));
        replay();
        assertFalse(resourceStorageDao.exists(ResourcePath.get("a"), 1));
        

        // Path exists when a child resource with a bucket exists
        index(ResourcePath.get("a", "b", "bucket"));
        replay();
        assertTrue(resourceStorageDao.exists(ResourcePath.get("a"), 1));
        

        // Path exists when the resource with a bucket exists
        index(ResourcePath.get("a", "bucket"));
        replay();
        assertTrue(resourceStorageDao.exists(ResourcePath.get("a"), 0));

        verify(searcher, atLeastOnce()).search(any(ResourcePath.class), anyInt());
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

        verify(searcher, atLeastOnce()).search(any(ResourcePath.class), anyInt());
    }

    @Test
    public void children() throws StorageException {
        // Children are empty when the resource id does not exist
        replay();
        assertEquals(0, resourceStorageDao.children(ResourcePath.get("should", "not", "exist"), 1).size());
        

        // Children are empty when there are no child resources
        index(ResourcePath.get("a"));
        replay();
        assertEquals(0, resourceStorageDao.children(ResourcePath.get("a"), 1).size());
        

        // Child exists when the is a child resource
        index(ResourcePath.get("a", "b", "bucket"));
        replay();
        Set<ResourcePath> children = resourceStorageDao.children(ResourcePath.get("a"), 1);
        assertEquals(1, children.size());
        assertEquals(ResourcePath.get("a", "b"), children.iterator().next());
        

        // Same call but specifying the depth
        index(ResourcePath.get("a", "b", "bucket"));
        replay();
        children = resourceStorageDao.children(ResourcePath.get("a"), 1);
        assertEquals(1, children.size());
        assertEquals(ResourcePath.get("a", "b"), children.iterator().next());
        

        // Only returns the next level
        index(ResourcePath.get("a", "b", "c", "bucket"));
        replay();
        children = resourceStorageDao.children(ResourcePath.get("a"), 2);
        assertEquals(1, children.size());
        assertEquals(ResourcePath.get("a", "b"), children.iterator().next());
        

        // No children when depth is 0
        index(ResourcePath.get("a", "b", "bucket"));
        replay();
        children = resourceStorageDao.children(ResourcePath.get("a"), 0);
        assertEquals(0, children.size());

        verify(searcher, atLeastOnce()).search(any(ResourcePath.class), anyInt());
    }

    @Test
    public void getAttributes() throws StorageException {
        // Attributes are empty when the resource does not exist
        replay();
        assertEquals(0, resourceStorageDao.getAttributes(ResourcePath.get("should", "not", "exist")).size());
        

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

        verify(searcher, atLeastOnce()).search(any(ResourcePath.class), anyInt());
    }

    @Test
    public void getResponseTimeAttributes() throws StorageException {
        index(ResourcePath.get(ResourceTypeUtils.RESPONSE_DIRECTORY, "127.0.0.1", "strafeping"), Sets.newHashSet("ping1", "ping2"));
        replay();
        Set<OnmsAttribute> attributes = resourceStorageDao.getAttributes(ResourcePath.get(ResourceTypeUtils.RESPONSE_DIRECTORY, "127.0.0.1"));

        // Response time metrics should be treated as a single attribute
        assertEquals(1, attributes.size());
        assertEquals("strafeping", attributes.iterator().next().getName());

        verify(searcher, atLeastOnce()).search(any(ResourcePath.class), anyInt());
    }

    private void index(ResourcePath path) {
        index(path, Sets.newHashSet());
    }

    private void index(ResourcePath path, Set<String> metrics) {
        if(metrics.isEmpty()) {
            metrics.add("fake"); // we must have a metric to show up
        }
        indexedPaths.put(path, metrics);
    }

    private void replay() throws StorageException {
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                ResourcePath resourcePath = invocation.getArgument(0);
                int depth = invocation.getArgument(1);

                String regex = toSearchRegex(resourcePath, depth + 1);
                Set<Metric> metrics = new HashSet<>();
                for (Entry<ResourcePath, Set<String>> entry : indexedPaths.entrySet()) {
                    Set<Tag> attributes = new HashSet<>();
                    if (Pattern.matches(regex, toResourceId(entry.getKey()))) { // find all paths that match our regex
                        for(String name : entry.getValue()) { // build the metric
                            ImmutableMetric.MetricBuilder metric =
                                    ImmutableMetric.builder()
                                    .intrinsicTag(IntrinsicTagNames.resourceId, toResourceId(entry.getKey()))
                                    .intrinsicTag(IntrinsicTagNames.name, name);
                            attributes.forEach(metric::metaTag);
                            metrics.add(metric.build());
                        }
                    }
                }

                return new HashSet<>(metrics);
            }
            
        }).when(searcher).search(any(ResourcePath.class), anyInt());
    }
}
