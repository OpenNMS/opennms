/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.support;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.opennms.netmgt.mock.MockResourceType;
import org.opennms.netmgt.model.AttributeStatistic;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.test.ThrowableAnticipator;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class TopNAttributeStatisticVisitorTest extends TestCase {
    
    public void testAfterPropertiesSet() throws Exception {
        BottomNAttributeStatisticVisitor visitor = new TopNAttributeStatisticVisitor();
        visitor.setCount(20);
        visitor.afterPropertiesSet();
    }


    public void testAfterPropertiesSetNoCount() throws Exception {
        BottomNAttributeStatisticVisitor visitor = new TopNAttributeStatisticVisitor();

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property count must be set to a non-null value"));
        
        visitor.setCount(null);

        try {
            visitor.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    public void testVisit() throws Exception {
        BottomNAttributeStatisticVisitor visitor = new TopNAttributeStatisticVisitor();
        visitor.setCount(20);
        visitor.afterPropertiesSet();
        
        Map<OnmsAttribute, Double> attributes = new HashMap<OnmsAttribute, Double>();
        attributes.put(new MockAttribute("foo"), 0.0);
        new OnmsResource("1", "Node One", new MockResourceType(), attributes.keySet());
        
        for (Entry<OnmsAttribute, Double> entry : attributes.entrySet()) {
            visitor.visit(entry.getKey(), entry.getValue());
        }
    }
    
    public void testVisitWithNull() throws Exception {
        BottomNAttributeStatisticVisitor visitor = new TopNAttributeStatisticVisitor();
        visitor.setCount(20);
        visitor.afterPropertiesSet();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("attribute argument must not be null"));
        
        try {
            visitor.visit(null, 0.0);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testVisitGetResults() throws Exception {
        BottomNAttributeStatisticVisitor visitor = new TopNAttributeStatisticVisitor();
        visitor.setCount(20);
        visitor.afterPropertiesSet();
        
        Map<OnmsAttribute, Double> attributes = new HashMap<OnmsAttribute, Double>();
        attributes.put(new MockAttribute("foo"), 0.0);
        new OnmsResource("1", "Node One", new MockResourceType(), attributes.keySet());
        
        for (Entry<OnmsAttribute, Double> entry : attributes.entrySet()) {
            visitor.visit(entry.getKey(), entry.getValue());
        }

        SortedSet<AttributeStatistic> top = visitor.getResults();
        assertNotNull("topN list should not be null", top);
        assertEquals("topN list size", 1, top.size());
        
        int i = 0;
        for (AttributeStatistic stat : top) { 
            assertEquals("topN[" + i + "] value", 0.0, stat.getStatistic());
        }
    }
    
    public void testVisitGetResultsSameValue() throws Exception {
        BottomNAttributeStatisticVisitor visitor = new TopNAttributeStatisticVisitor();
        visitor.setCount(20);
        visitor.afterPropertiesSet();
        
        Map<OnmsAttribute, Double> attributes = new HashMap<OnmsAttribute, Double>();
        for (int i = 0; i < 5; i++) {
            attributes.put(new MockAttribute("foo"), 0.0);
        }
        new OnmsResource("1", "Node One", new MockResourceType(), attributes.keySet());
        
        for (Entry<OnmsAttribute, Double> entry : attributes.entrySet()) {
            visitor.visit(entry.getKey(), entry.getValue());
        }
        
        SortedSet<AttributeStatistic> top = visitor.getResults();
        assertNotNull("topN list should not be null", top);
        assertEquals("topN list size", 5, top.size());

        int i = 0;
        for (AttributeStatistic stat : top) { 
            assertEquals("topN[" + i + "] value", 0.0, stat.getStatistic());
            i++;
        }
    }
    
    public void testVisitGetResultsDifferentValues() throws Exception {
        BottomNAttributeStatisticVisitor visitor = new TopNAttributeStatisticVisitor();
        visitor.setCount(20);
        visitor.afterPropertiesSet();
        
        Map<OnmsAttribute, Double> attributes = new HashMap<OnmsAttribute, Double>();
        for (int i = 0; i < 5; i++) {
            attributes.put(new MockAttribute("foo"), 0.0 + i);
        }
        new OnmsResource("1", "Node One", new MockResourceType(), attributes.keySet());
        
        for (Entry<OnmsAttribute, Double> entry : attributes.entrySet()) {
            visitor.visit(entry.getKey(), entry.getValue());
        }
        
        SortedSet<AttributeStatistic> top = visitor.getResults();
        assertNotNull("topN list should not be null", top);
        assertEquals("topN list size", 5, top.size());

        int i = 0;
        for (AttributeStatistic stat : top) { 
            assertEquals("topN[" + i + "] value", 4.0 - i, stat.getStatistic());
            i++;
        }
    }
    
    public void testVisitGetResultsLimitedByCount() throws Exception {
        BottomNAttributeStatisticVisitor visitor = new TopNAttributeStatisticVisitor();
        visitor.setCount(20);
        visitor.afterPropertiesSet();
        
        Map<OnmsAttribute, Double> attributes = new HashMap<OnmsAttribute, Double>();
        for (int i = 0; i < 100; i++) {
            attributes.put(new MockAttribute("foo"), 0.0 + i);
        }
        new OnmsResource("1", "Node One", new MockResourceType(), attributes.keySet());
        
        for (Entry<OnmsAttribute, Double> entry : attributes.entrySet()) {
            visitor.visit(entry.getKey(), entry.getValue());
        }

        SortedSet<AttributeStatistic> top = visitor.getResults();
        assertNotNull("topN list should not be null", top);
        assertEquals("topN list size", 20, top.size());

        int i = 0;
        for (AttributeStatistic stat : top) { 
            assertEquals("topN[" + i + "] value", 99.0 - i, stat.getStatistic());
            i++;
        }
    }
    
    
    public class MockAttribute implements OnmsAttribute {
        private String m_name;
        private OnmsResource m_resource;
        
        public MockAttribute(String name) {
            m_name = name;
        }
        
        /**
         * @see org.opennms.netmgt.model.OnmsAttribute#getName()
         */
        @Override
        public String getName() {
            return m_name;
        }

        /**
         * @see org.opennms.netmgt.model.OnmsAttribute#getResource()
         */
        @Override
        public OnmsResource getResource() {
            return m_resource;
        }

        /**
         * @see org.opennms.netmgt.model.OnmsAttribute#setResource(org.opennms.netmgt.model.OnmsResource)
         */
        @Override
        public void setResource(OnmsResource resource) {
            m_resource = resource;
        }
        
    }
}
