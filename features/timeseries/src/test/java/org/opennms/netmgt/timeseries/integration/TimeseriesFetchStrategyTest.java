/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.timeseries.api.TimeSeriesStorage;
import org.opennms.netmgt.timeseries.api.domain.Aggregation;
import org.opennms.netmgt.timeseries.api.domain.Metric;
import org.opennms.netmgt.timeseries.api.domain.Sample;
import org.opennms.netmgt.timeseries.api.domain.StorageException;
import org.opennms.netmgt.timeseries.api.domain.TimeSeriesFetchRequest;
import org.opennms.netmgt.timeseries.integration.TimeseriesFetchStrategy.LateAggregationParams;
import org.opennms.newts.api.Measurement;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Results.Row;
import org.opennms.newts.api.Timestamp;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class TimeseriesFetchStrategyTest {

    private final static long START_TIME = 1431047069000L - (300 * 1000);
    private final static long END_TIME = 1431047069000L -1;
    private final static long STEP = (300 * 1000);

    private ResourceDao resourceDao;
    private TimeSeriesStorage timeSeriesStorage;

    private TimeseriesFetchStrategy fetchStrategy;

    private Map<ResourceId, OnmsResource> m_resources = Maps.newHashMap();


    @Before
    public void setUp() {
        resourceDao = EasyMock.createNiceMock(ResourceDao.class);
        timeSeriesStorage = EasyMock.createNiceMock(TimeSeriesStorage.class);
 
        fetchStrategy = new TimeseriesFetchStrategy();
        fetchStrategy.setResourceDao(resourceDao);
        fetchStrategy.setTimeseriesStorage(timeSeriesStorage);
    }

    @After
    public void tearDown() {
        EasyMock.verify(resourceDao, timeSeriesStorage);
    }

    @Test
    public void canRetrieveAttributeWhenFallbackAttributeIsSet() throws Exception {
        createMockResource("icmplocalhost", "icmp", "127.0.0.1");
        replay();

        Source sourceToBeFetched = new Source();
        sourceToBeFetched.setResourceId("nodeSource[NODES:1505998205].responseTime[127.0.0.1]");
        sourceToBeFetched.setAttribute("icmp");
        sourceToBeFetched.setFallbackAttribute("willNotBeFound");
        sourceToBeFetched.setAggregation("AVERAGE");
        sourceToBeFetched.setLabel("icmp");

        FetchResults fetchResults = fetchStrategy.fetch(START_TIME, END_TIME, STEP, 0, null, null, Lists.newArrayList(sourceToBeFetched), false);
        assertEquals(1, fetchResults.getColumns().keySet().size());
        assertTrue(fetchResults.getColumns().containsKey("icmp"));
        assertEquals(1, fetchResults.getTimestamps().length);
    }

    @Test
    public void canRetrieveFallbackAttributeWhenAttributeNotFound() throws Exception {
        createMockResource("icmplocalhost", "icmp", "127.0.0.1");
        replay();

        Source sourceToBeFetched = new Source();
        sourceToBeFetched.setResourceId("nodeSource[NODES:1505998205].responseTime[127.0.0.1]");
        sourceToBeFetched.setAttribute("willNotBeFound");
        sourceToBeFetched.setFallbackAttribute("icmp");
        sourceToBeFetched.setAggregation("AVERAGE");
        sourceToBeFetched.setLabel("icmp");

        FetchResults fetchResults = fetchStrategy.fetch(START_TIME, END_TIME, STEP, 0, null, null, Lists.newArrayList(sourceToBeFetched), false);
        assertEquals(1, fetchResults.getColumns().keySet().size());
        assertTrue(fetchResults.getColumns().containsKey("icmp"));
        assertEquals(1, fetchResults.getTimestamps().length);
    }

    @Test
    public void cannotRetrieveUnknownAttributeAndUnknownFallbackAttribute() throws StorageException {
        createMockResource("icmplocalhost", "shouldNotBeFound", "127.0.0.1", false);
        replay();

        Source sourceToBeFetched = new Source();
        sourceToBeFetched.setResourceId("nodeSource[NODES:1505998205].responseTime[127.0.0.1]");
        sourceToBeFetched.setAttribute("willNotBeFound");
        sourceToBeFetched.setFallbackAttribute("willNotBeFoundToo");
        sourceToBeFetched.setAggregation("AVERAGE");
        sourceToBeFetched.setLabel("icmp");

        FetchResults fetchResults = fetchStrategy.fetch(START_TIME, END_TIME, STEP, 0, null, null, Lists.newArrayList(sourceToBeFetched), false);
        assertNull(fetchResults);
    }

    @Test
    public void testFetch() throws Exception {
        List<Source> sources = Lists.newArrayList(
            createMockResource("icmplocalhost", "icmp", "127.0.0.1"),
            createMockResource("snmplocalhost", "snmp", "127.0.0.1"),
            createMockResource("snmp192", "snmp", "192.168.0.1")
        );
        replay();

        FetchResults fetchResults = fetchStrategy.fetch(START_TIME, END_TIME, STEP, 0, null, null, sources, false);
        assertEquals(3, fetchResults.getColumns().keySet().size());
        assertTrue(fetchResults.getColumns().containsKey("icmplocalhost"));
        assertTrue(fetchResults.getColumns().containsKey("snmplocalhost"));
        assertTrue(fetchResults.getColumns().containsKey("snmp192"));
        assertEquals(1, fetchResults.getTimestamps().length);
    }

    @Test
    public void testFetchWithDuplicateResources() throws Exception {
        List<Source> sources = Lists.newArrayList(
            createMockResource("icmp", "icmp", "127.0.0.1"),
            createMockResource("icmp", "icmp", "192.168.0.1")
        );
        replay();

        FetchResults fetchResults = fetchStrategy.fetch(START_TIME, END_TIME, STEP, 0, null, null, sources, false);
        // It's not possible to fetch multiple resources with the same label, we should only get 1 ICMP result
        assertEquals(1, fetchResults.getColumns().keySet().size());
    }

    @Test
    public void canLimitStepSize() {
        replay();
        // Request a step size smaller than the lower bound
        LateAggregationParams lag = TimeseriesFetchStrategy.getLagParams(TimeseriesFetchStrategy.MIN_STEP_MS - 1,
                null, null);
        assertEquals(TimeseriesFetchStrategy.MIN_STEP_MS, lag.step);
    }

    @Test
    public void canCalculateLagParams() {
        replay();

        // Supply sane values and make sure the same values are returned
        LateAggregationParams lag = TimeseriesFetchStrategy.getLagParams(300*1000L, 150*1000L, 450*1000L);
        assertEquals(300*1000L, lag.step);
        assertEquals(150*1000L, lag.interval);

        // Supply a step that is not a multiple of the interval, make sure this is corrected
        lag = TimeseriesFetchStrategy.getLagParams(310*1000L, 150*1000L, 450*1000L);
        assertEquals(310000L, lag.step);
        assertEquals(155000L, lag.interval);

        // Supply an interval that is much larger than the step
        lag = TimeseriesFetchStrategy.getLagParams(300*1000L, 1500*1000L, 45000*1000L);
        assertEquals(300*1000L, lag.step);
        // Interval should be reduced
        assertEquals(150*1000L, lag.interval);
    }

    @Test
    public void canRetrieveValuesByDatasource() throws StorageException {
        List<Source> sources = Collections.singletonList(
                createMockResource("ping1Micro", "strafeping",  "ping1", "127.0.0.1", true));
        replay();

        FetchResults fetchResults = fetchStrategy.fetch(START_TIME, END_TIME, STEP, 0, null, null, sources, false);
        assertEquals(1, fetchResults.getColumns().keySet().size());
        assertTrue(fetchResults.getColumns().containsKey("ping1Micro"));
    }

    public Source createMockResource(final String label, final String attr, final String node) throws StorageException {
        return createMockResource(label, attr, node, true);
    }

    public Source createMockResource(final String label, final String attr, final String node, boolean expect) throws StorageException {
        return createMockResource(label, attr, null, node, expect);
    }

    public Source createMockResource(final String label, final String attr, final String ds, final String node, boolean expect) throws StorageException {
        OnmsResourceType nodeType = EasyMock.createMock(OnmsResourceType.class);
        EasyMock.expect(nodeType.getName()).andReturn("nodeSource").anyTimes();
        EasyMock.expect(nodeType.getLabel()).andReturn("nodeSourceTypeLabel").anyTimes();
        EasyMock.replay(nodeType);

        OnmsResourceType type = EasyMock.createMock(OnmsResourceType.class);
        EasyMock.expect(type.getName()).andReturn("newtsTypeName").anyTimes();
        EasyMock.expect(type.getLabel()).andReturn("newtsTypeLabel").anyTimes();
        EasyMock.replay(type);

        final int nodeId = node.hashCode();
        final String newtsResourceId = "response:" + node + ":" + attr;
        final ResourceId parentId = ResourceId.get("nodeSource", "NODES:" + nodeId);
        final ResourceId resourceId = parentId.resolve("responseTime", node);
        OnmsResource parent = m_resources.get(parentId);
        if (parent == null) {
            parent = new OnmsResource("NODES:" + nodeId, ""+nodeId, nodeType, Sets.newHashSet(), ResourcePath.get("foo"));
            final OnmsNode entity = new OnmsNode();
            entity.setId(nodeId);
            entity.setForeignSource("NODES");
            entity.setForeignId(""+nodeId);
            entity.setLabel(""+nodeId);
            parent.setEntity(entity);
            m_resources.put(parentId, parent);
        }
        OnmsResource resource = m_resources.get(resourceId);
        if (resource == null) {
            resource = new OnmsResource(attr, label, type, Sets.newHashSet(), ResourcePath.get("foo"));
            resource.setParent(parent);
            m_resources.put(resourceId, resource);
        }
        Set<OnmsAttribute> attributes = resource.getAttributes();
        attributes.add(new RrdGraphAttribute(attr, "", newtsResourceId));

        List<Sample> results = new ArrayList<>();

        Resource res = new Resource(newtsResourceId);
        Row<Measurement> row = new Row<Measurement>(Timestamp.fromEpochSeconds(0), res);
        Measurement measurement = new Measurement(Timestamp.fromEpochSeconds(0), res, label, 0.0d);
        row.addElement(measurement);


        String name = ds != null ? ds : attr;
        Metric metric = Metric.builder()
                .tag(CommonTagNames.resourceId, newtsResourceId)
                .tag(CommonTagNames.name, name)
                .tag(Metric.MandatoryTag.mtype.name(), Metric.Mtype.gauge.name())
                .tag(Metric.MandatoryTag.unit.name(), CommonTagValues.unknown)
                .build();
        Sample sample = Sample.builder()
                .metric(metric)
                .time(Instant.ofEpochMilli(START_TIME))
                .value(33.0)
                .build();

        results.add(sample);

        TimeSeriesFetchRequest request = TimeSeriesFetchRequest.builder()
                .metric(metric)
                .aggregation(Aggregation.NONE) // we do the aggregation in the JVM
                .start(Instant.ofEpochMilli(START_TIME))
                .end(Instant.ofEpochMilli(END_TIME))
                .step(Duration.ofMillis(STEP))
                .build();

        if (expect) {
            EasyMock.expect(timeSeriesStorage.getTimeseries(request)).andReturn(results);
        }

        final Source source = new Source();
        source.setAggregation("AVERAGE");
        source.setAttribute(attr);
        source.setDataSource(ds);
        source.setLabel(label);
        source.setResourceId(resourceId.toString());
        source.setTransient(false);
        return source;
    }

    private void replay() {
        for (Entry<ResourceId, OnmsResource> entry : m_resources.entrySet()) {
            EasyMock.expect(resourceDao.getResourceById(entry.getKey())).andReturn(entry.getValue()).anyTimes();
        }

        EasyMock.replay(resourceDao, timeSeriesStorage);
    }
}
