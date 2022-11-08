/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.After;
import org.junit.Test;
import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.api.MeasurementFetchStrategy;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.mock.MockResourceType;
import org.opennms.netmgt.model.AttributeStatisticVisitor;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.model.StringPropertyAttribute;
import org.opennms.test.ThrowableAnticipator;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class RrdStatisticAttributeVisitorTest {
    private MeasurementFetchStrategy m_fetchStrategy = mock(MeasurementFetchStrategy.class);
    private Long m_startTime = System.currentTimeMillis();
    private Long m_endTime = m_startTime + (24 * 60 * 60 * 1000); // one day
    private AttributeStatisticVisitor m_statisticVisitor = mock(AttributeStatisticVisitor.class);
    
    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(m_fetchStrategy);
        verifyNoMoreInteractions(m_statisticVisitor);
    }

    @Test
    public void testAfterPropertiesSet() throws Exception {
        RrdStatisticAttributeVisitor attributeVisitor = new RrdStatisticAttributeVisitor();
        attributeVisitor.setFetchStrategy(m_fetchStrategy);
        attributeVisitor.setConsolidationFunction("AVERAGE");
        attributeVisitor.setStartTime(m_startTime);
        attributeVisitor.setEndTime(m_endTime);
        attributeVisitor.setStatisticVisitor(m_statisticVisitor);
        attributeVisitor.afterPropertiesSet();
    }

    @Test
    public void testAfterPropertiesSetNoStatisticVisitor() throws Exception {
        RrdStatisticAttributeVisitor attributeVisitor = new RrdStatisticAttributeVisitor();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property statisticVisitor must be set to a non-null value"));
        
        attributeVisitor.setFetchStrategy(m_fetchStrategy);
        attributeVisitor.setConsolidationFunction("AVERAGE");
        attributeVisitor.setStartTime(m_startTime);
        attributeVisitor.setEndTime(m_endTime);
        attributeVisitor.setStatisticVisitor(null);

        try {
            attributeVisitor.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testAfterPropertiesSetNoConsolidationFunction() throws Exception {
        RrdStatisticAttributeVisitor attributeVisitor = new RrdStatisticAttributeVisitor();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property consolidationFunction must be set to a non-null value"));

        attributeVisitor.setFetchStrategy(m_fetchStrategy);
        attributeVisitor.setConsolidationFunction(null);
        attributeVisitor.setStartTime(m_startTime);
        attributeVisitor.setEndTime(m_endTime);
        attributeVisitor.setStatisticVisitor(m_statisticVisitor);

        try {
            attributeVisitor.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testAfterPropertiesSetNoRrdDao() throws Exception {
        RrdStatisticAttributeVisitor attributeVisitor = new RrdStatisticAttributeVisitor();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property fetchStrategy must be set to a non-null value"));

        attributeVisitor.setFetchStrategy(null);
        attributeVisitor.setConsolidationFunction("AVERAGE");
        attributeVisitor.setStartTime(m_startTime);
        attributeVisitor.setEndTime(m_endTime);
        attributeVisitor.setStatisticVisitor(m_statisticVisitor);

        try {
            attributeVisitor.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testAfterPropertiesSetNoStartTime() throws Exception {
        RrdStatisticAttributeVisitor attributeVisitor = new RrdStatisticAttributeVisitor();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property startTime must be set to a non-null value"));

        attributeVisitor.setFetchStrategy(m_fetchStrategy);
        attributeVisitor.setConsolidationFunction("AVERAGE");
        attributeVisitor.setStartTime(null);
        attributeVisitor.setEndTime(m_endTime);
        attributeVisitor.setStatisticVisitor(m_statisticVisitor);

        try {
            attributeVisitor.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testAfterPropertiesSetNoEndTime() throws Exception {
        RrdStatisticAttributeVisitor attributeVisitor = new RrdStatisticAttributeVisitor();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property endTime must be set to a non-null value"));

        attributeVisitor.setFetchStrategy(m_fetchStrategy);
        attributeVisitor.setConsolidationFunction("AVERAGE");
        attributeVisitor.setStartTime(m_startTime);
        attributeVisitor.setEndTime(null);
        attributeVisitor.setStatisticVisitor(m_statisticVisitor);

        try {
            attributeVisitor.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testVisitWithRrdAttribute() throws Exception {
        RrdStatisticAttributeVisitor attributeVisitor = new RrdStatisticAttributeVisitor();
        attributeVisitor.setFetchStrategy(m_fetchStrategy);
        attributeVisitor.setConsolidationFunction("AVERAGE");
        attributeVisitor.setStartTime(m_startTime);
        attributeVisitor.setEndTime(m_endTime);
        attributeVisitor.setStatisticVisitor(m_statisticVisitor);
        attributeVisitor.afterPropertiesSet();

        MockResourceType resourceType = new MockResourceType();
        resourceType.setName("interfaceSnmp");
        OnmsAttribute attribute = new RrdGraphAttribute("ifInOctets", "something", "something else");
        attribute.setResource(new OnmsResource("1", "Node One", resourceType, Collections.singleton(attribute), ResourcePath.get("foo")));
        Source source = new Source();
        source.setLabel("result");
        source.setResourceId(attribute.getResource().getId().toString());
        source.setAttribute(attribute.getName());
        source.setAggregation(attributeVisitor.getConsolidationFunction().toUpperCase());
        FetchResults results = new FetchResults(new long[] {m_startTime},
                                                Collections.singletonMap("result", new double[] {1.0}),
                                                m_endTime - m_startTime,
                                                Collections.emptyMap(),
                                                null);

        final var sourceList = Collections.singletonList(source);
        when(m_fetchStrategy.fetch(m_startTime,
                                     m_endTime,
                                     1,
                                     0,
                                     null,
                                     null,
                                     sourceList,
                                    false))
                .thenReturn(results);
        m_statisticVisitor.visit(attribute, 1.0);

        attributeVisitor.visit(attribute);

        verify(m_fetchStrategy, times(1)).fetch(m_startTime, m_endTime, 1, 0, null, null, sourceList, false);
        verify(m_statisticVisitor, times(2)).visit(any(OnmsAttribute.class), eq(1.0));
    }

    @Test
    public void testVisitWithNonRrdAttribute() throws Exception {
        RrdStatisticAttributeVisitor attributeVisitor = new RrdStatisticAttributeVisitor();
        attributeVisitor.setFetchStrategy(m_fetchStrategy);
        attributeVisitor.setConsolidationFunction("AVERAGE");
        attributeVisitor.setStartTime(m_startTime);
        attributeVisitor.setEndTime(m_endTime);
        attributeVisitor.setStatisticVisitor(m_statisticVisitor);
        attributeVisitor.afterPropertiesSet();

        MockResourceType resourceType = new MockResourceType();
        resourceType.setName("something other than interfaceSnmp");
        OnmsAttribute attribute = new StringPropertyAttribute("ifInOctets", "one billion octets!");
        attribute.setResource(new OnmsResource("1", "Node One", resourceType, Collections.singleton(attribute), ResourcePath.get("foo")));

        attributeVisitor.visit(attribute);
    }

    @Test
    public void testVisitWithNotANumberRrdAttribute() throws Exception {
        RrdStatisticAttributeVisitor attributeVisitor = new RrdStatisticAttributeVisitor();
        attributeVisitor.setFetchStrategy(m_fetchStrategy);
        attributeVisitor.setConsolidationFunction("AVERAGE");
        attributeVisitor.setStartTime(m_startTime);
        attributeVisitor.setEndTime(m_endTime);
        attributeVisitor.setStatisticVisitor(m_statisticVisitor);
        attributeVisitor.afterPropertiesSet();

        MockResourceType resourceType = new MockResourceType();
        resourceType.setName("somethingOtherThanInterfaceSnmp");
        OnmsAttribute attribute = new RrdGraphAttribute("ifInOctets", "something", "something else");
        attribute.setResource(new OnmsResource("1", "Node One", resourceType, Collections.singleton(attribute), ResourcePath.get("foo")));
        Source source = new Source();
        source.setLabel("result");
        source.setResourceId(attribute.getResource().getId().toString());
        source.setAttribute(attribute.getName());
        source.setAggregation(attributeVisitor.getConsolidationFunction().toUpperCase());
        FetchResults results = new FetchResults(new long[] {},
                                                Collections.singletonMap("result", new double[] {}),
                                                m_endTime - m_startTime,
                                                Collections.emptyMap(),
                                                null);
        final var sourceList = Collections.singletonList(source);
        when(m_fetchStrategy.fetch(m_startTime,
                                     m_endTime,
                                     1,
                                     0,
                                     null,
                                     null,
                                     sourceList,
                                    false))
                .thenReturn(results);

        attributeVisitor.visit(attribute);

        verify(m_fetchStrategy, times(1)).fetch(m_startTime, m_endTime, 1, 0, null, null, sourceList, false);
    }

    @Test
    public void testVisitTwice() throws Exception {
        final RrdStatisticAttributeVisitor attributeVisitor1 = new RrdStatisticAttributeVisitor();
        attributeVisitor1.setFetchStrategy(m_fetchStrategy);
        attributeVisitor1.setConsolidationFunction("AVERAGE");
        attributeVisitor1.setStartTime(m_startTime);
        attributeVisitor1.setEndTime(m_endTime);
        attributeVisitor1.setStatisticVisitor(m_statisticVisitor);
        attributeVisitor1.afterPropertiesSet();

        final RrdStatisticAttributeVisitor attributeVisitor2 = new RrdStatisticAttributeVisitor();
        attributeVisitor2.setFetchStrategy(m_fetchStrategy);
        attributeVisitor2.setConsolidationFunction("AVERAGE");
        attributeVisitor2.setStartTime(m_startTime);
        attributeVisitor2.setEndTime(m_endTime);
        attributeVisitor2.setStatisticVisitor(m_statisticVisitor);
        attributeVisitor2.afterPropertiesSet();

        MockResourceType resourceType = new MockResourceType();
        resourceType.setName("interfaceSnmp");

        OnmsAttribute attribute = new RrdGraphAttribute("ifInOctets", "something", "something else");
        attribute.setResource(new OnmsResource("1", "Node One", resourceType, Collections.singleton(attribute), ResourcePath.get("foo")));

        Source source = new Source();
        source.setLabel("result");
        source.setResourceId(attribute.getResource().getId().toString());
        source.setAttribute(attribute.getName());
        source.setAggregation(attributeVisitor1.getConsolidationFunction().toUpperCase());

        final var sourceList = Collections.singletonList(source);
        when(m_fetchStrategy.fetch(m_startTime,
                                     m_endTime,
                                     1,
                                     0,
                                     null,
                                     null,
                                     sourceList,
                                     false))
                .thenReturn(new FetchResults(new long[]{m_startTime},
                                            Collections.singletonMap("result", new double[]{1.0}),
                                            m_endTime - m_startTime,
                                            Collections.emptyMap(),
                                            null));
        m_statisticVisitor.visit(attribute, 1.0);

        when(m_fetchStrategy.fetch(m_startTime,
                                     m_endTime,
                                     1,
                                     0,
                                     null,
                                     null,
                                     sourceList,
                                     false))
                .thenReturn(new FetchResults(new long[]{m_startTime},
                                            Collections.singletonMap("result", new double[]{2.0}),
                                            m_endTime - m_startTime,
                                            Collections.emptyMap(),
                                            null));
        m_statisticVisitor.visit(attribute, 2.0);

        attributeVisitor1.visit(attribute);
        attributeVisitor2.visit(attribute);

        verify(m_fetchStrategy, times(2)).fetch(m_startTime, m_endTime, 1, 0, null, null, sourceList, false);
        verify(m_statisticVisitor, times(1)).visit(any(OnmsAttribute.class), eq(1.0));
        verify(m_statisticVisitor, times(3)).visit(any(OnmsAttribute.class), eq(2.0));
    }
}
