/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.measurements.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.impl.NewtsFetchStrategy.LateAggregationParams;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Measurement;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Results;
import org.opennms.newts.api.Results.Row;
import org.opennms.newts.api.SampleRepository;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.api.query.ResultDescriptor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class NewtsFetchStrategyTest {
    private Context m_context;
    private ResourceDao m_resourceDao;
    private SampleRepository m_sampleRepository;

    private NewtsFetchStrategy m_newtsFetchStrategy;

    private Map<ResourceId, OnmsResource> m_resources = Maps.newHashMap();

    private Capture<ResultDescriptor> lastCapturedResultDescriptor = new Capture<>();

    @Before
    public void setUp() throws Exception {
        m_context = new Context("test");
        m_resourceDao = EasyMock.createNiceMock(ResourceDao.class);
        m_sampleRepository = EasyMock.createNiceMock(SampleRepository.class);
 
        m_newtsFetchStrategy = new NewtsFetchStrategy();
        m_newtsFetchStrategy.setContext(m_context);
        m_newtsFetchStrategy.setResourceDao(m_resourceDao);
        m_newtsFetchStrategy.setSampleRepository(m_sampleRepository);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(m_resourceDao, m_sampleRepository);
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

        FetchResults fetchResults = m_newtsFetchStrategy.fetch(1431047069000L - (60 * 60 * 1000), 1431047069000L, 300 * 1000, 0, null, null, Lists.newArrayList(sourceToBeFetched), false);
        assertEquals(1, fetchResults.getColumns().keySet().size());
        assertTrue(fetchResults.getColumns().containsKey("icmplocalhost"));
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

        FetchResults fetchResults = m_newtsFetchStrategy.fetch(1431047069000L - (60 * 60 * 1000), 1431047069000L, 300 * 1000, 0, null, null, Lists.newArrayList(sourceToBeFetched), false);
        assertEquals(1, fetchResults.getColumns().keySet().size());
        assertTrue(fetchResults.getColumns().containsKey("icmplocalhost"));
        assertEquals(1, fetchResults.getTimestamps().length);
    }

    @Test
    public void cannotRetrieveUnknownAttributeAndUnknownFallbackAttribute() {
        createMockResource("icmplocalhost", "shouldNotBeFound", "127.0.0.1", false);
        replay();

        Source sourceToBeFetched = new Source();
        sourceToBeFetched.setResourceId("nodeSource[NODES:1505998205].responseTime[127.0.0.1]");
        sourceToBeFetched.setAttribute("willNotBeFound");
        sourceToBeFetched.setFallbackAttribute("willNotBeFoundToo");
        sourceToBeFetched.setAggregation("AVERAGE");
        sourceToBeFetched.setLabel("icmp");

        FetchResults fetchResults = m_newtsFetchStrategy.fetch(1431047069000L - (60 * 60 * 1000), 1431047069000L, 300 * 1000, 0, null, null, Lists.newArrayList(sourceToBeFetched), false);
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

        FetchResults fetchResults = m_newtsFetchStrategy.fetch(1431047069000L - (60 * 60 * 1000), 1431047069000L, 300 * 1000, 0, null, null, sources, false);
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

        FetchResults fetchResults = m_newtsFetchStrategy.fetch(1431047069000L - (60 * 60 * 1000), 1431047069000L, 300 * 1000, 0, null, null, sources, false);
        // It's not possible to fetch multiple resources with the same label, we should only get 1 ICMP result
        assertEquals(1, fetchResults.getColumns().keySet().size());
    }

    @Test
    public void canLimitStepSize() {
        replay();
        // Request a step size smaller than the lower bound
        LateAggregationParams lag = NewtsFetchStrategy.getLagParams(NewtsFetchStrategy.MIN_STEP_MS - 1,
                null, null);
        assertEquals(NewtsFetchStrategy.MIN_STEP_MS, lag.step);
    }

    @Test
    public void canCalculateLagParams() {
        replay();

        // Supply sane values and make sure the same values are returned
        LateAggregationParams lag = NewtsFetchStrategy.getLagParams(300*1000L, 150*1000L, 450*1000L);
        assertEquals(300*1000L, lag.step);
        assertEquals(150*1000L, lag.interval);
        assertEquals(450*1000L, lag.heartbeat);

        // Supply a step that is not a multiple of the interval, make sure this is corrected
        lag = NewtsFetchStrategy.getLagParams(310*1000L, 150*1000L, 450*1000L);
        assertEquals(310000L, lag.step);
        assertEquals(155000L, lag.interval);
        assertEquals(465000L, lag.heartbeat);

        // Supply an interval that is much larger than the step
        lag = NewtsFetchStrategy.getLagParams(300*1000L, 1500*1000L, 45000*1000L);
        assertEquals(300*1000L, lag.step);
        // Interval should be reduced
        assertEquals(150*1000L, lag.interval);
        // But the hearbeat should stay the same
        assertEquals(45000*1000L, lag.heartbeat);
    }

    @Test
    public void canRetrieveValuesByDatasource() {
        List<Source> sources = Collections.singletonList(
                createMockResource("ping1Micro", "strafeping",  "ping1", "127.0.0.1", true));
        replay();

        FetchResults fetchResults = m_newtsFetchStrategy.fetch(1431047069000L - (60 * 60 * 1000), 1431047069000L,
                300 * 1000, 0, null, null, sources, false);
        assertEquals(1, fetchResults.getColumns().keySet().size());
        assertTrue(fetchResults.getColumns().containsKey("ping1Micro"));

        // Verify that the name of the source matches the name of the DS provided in the source definition
        assertEquals("ping1", lastCapturedResultDescriptor.getValue().getDatasources().get("ping1Micro").getSource());
    }

    public Source createMockResource(final String label, final String attr, final String node) {
        return createMockResource(label, attr, node, true);
    }

    public Source createMockResource(final String label, final String attr, final String node, boolean expect) {
        return createMockResource(label, attr, null, node, expect);
    }

    public Source createMockResource(final String label, final String attr, final String ds, final String node, boolean expect) {
        OnmsResourceType type = EasyMock.createNiceMock(OnmsResourceType.class);

        final int nodeId = node.hashCode();
        final String newtsResourceId = "response:" + node + ":" + attr;
        final ResourceId resourceId = ResourceId.get("nodeSource", "NODES:" + nodeId).resolve("responseTime", node);
        OnmsResource resource = m_resources.get(resourceId);
        if (resource == null) {
            resource = new OnmsResource(attr, label, type, Sets.newHashSet(), ResourcePath.get("foo"));
            m_resources.put(resourceId, resource);
        }
        Set<OnmsAttribute> attributes = resource.getAttributes();
        attributes.add(new RrdGraphAttribute(attr, "", newtsResourceId));

        Results<Measurement> results = new Results<>();
        Resource res = new Resource(newtsResourceId);
        Row<Measurement> row = new Row<Measurement>(Timestamp.fromEpochSeconds(0), res);
        Measurement measurement = new Measurement(Timestamp.fromEpochSeconds(0), res, label, 0.0d);
        row.addElement(measurement);
        results.addRow(row);

        if (expect) {
            EasyMock.expect(m_sampleRepository.select(
                    EasyMock.eq(m_context), EasyMock.eq(res), EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.capture(lastCapturedResultDescriptor), EasyMock.anyObject(), EasyMock.anyObject()
            )).andReturn(results);
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
            EasyMock.expect(m_resourceDao.getResourceById(entry.getKey())).andReturn(entry.getValue());
        }

        EasyMock.replay(m_resourceDao, m_sampleRepository);
    }
}
