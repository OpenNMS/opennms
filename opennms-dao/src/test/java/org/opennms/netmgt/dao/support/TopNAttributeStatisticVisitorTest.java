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

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.opennms.netmgt.mock.MockResourceType;
import org.opennms.netmgt.model.AttributeStatistic;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourcePath;
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
        OnmsResource resource = new OnmsResource("1", "Node One", new MockResourceType(), attributes.keySet(), ResourcePath.get("foo"));
        resource.getAttributes();

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
        OnmsResource resource = new OnmsResource("1", "Node One", new MockResourceType(), attributes.keySet(), ResourcePath.get("foo"));
        resource.getAttributes();

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
        OnmsResource resource = new OnmsResource("1", "Node One", new MockResourceType(), attributes.keySet(), ResourcePath.get("foo"));
        resource.getAttributes();

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
        OnmsResource resource = new OnmsResource("1", "Node One", new MockResourceType(), attributes.keySet(), ResourcePath.get("foo"));
        resource.getAttributes();

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
        OnmsResource resource = new OnmsResource("1", "Node One", new MockResourceType(), attributes.keySet(), ResourcePath.get("foo"));
        resource.getAttributes();

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
