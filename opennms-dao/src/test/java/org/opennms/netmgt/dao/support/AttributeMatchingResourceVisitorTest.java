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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;
import org.opennms.netmgt.mock.MockResourceType;
import org.opennms.netmgt.model.AttributeVisitor;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.test.ThrowableAnticipator;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class AttributeMatchingResourceVisitorTest {
    private AttributeVisitor m_attributeVisitor = mock(AttributeVisitor.class);

    @Test
    public void testAfterPropertiesSet() throws Exception {
        AttributeMatchingResourceVisitor resourceVisitor = new AttributeMatchingResourceVisitor();
        resourceVisitor.setAttributeVisitor(m_attributeVisitor);
        resourceVisitor.setAttributeMatch("ifInOctets");
        resourceVisitor.afterPropertiesSet();
    }

    @Test
    public void testAfterPropertiesSetNoAttributeVisitor() throws Exception {
        AttributeMatchingResourceVisitor resourceVisitor = new AttributeMatchingResourceVisitor();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property attributeVisitor must be set to a non-null value"));
        
        resourceVisitor.setAttributeVisitor(null);
        resourceVisitor.setAttributeMatch("ifInOctets");

        try {
            resourceVisitor.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testAfterPropertiesSetNoResourceTypeMatch() throws Exception {
        AttributeMatchingResourceVisitor resourceVisitor = new AttributeMatchingResourceVisitor();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property attributeMatch must be set to a non-null value"));

        resourceVisitor.setAttributeVisitor(m_attributeVisitor);
        resourceVisitor.setAttributeMatch(null);

        try {
            resourceVisitor.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testVisitWithMatch() throws Exception {
        AttributeMatchingResourceVisitor resourceVisitor = new AttributeMatchingResourceVisitor();
        resourceVisitor.setAttributeVisitor(m_attributeVisitor);
        resourceVisitor.setAttributeMatch("ifInOctets");
        resourceVisitor.afterPropertiesSet();

        MockResourceType resourceType = new MockResourceType();
        resourceType.setName("interfaceSnmp");
        OnmsAttribute attribute = new RrdGraphAttribute("ifInOctets", "something", "something else");
        OnmsResource resource = new OnmsResource("1", "Node One", resourceType, Collections.singleton(attribute), ResourcePath.get("foo"));
        m_attributeVisitor.visit(attribute);

        doNothing().when(m_attributeVisitor).visit(any(OnmsAttribute.class));
        resourceVisitor.visit(resource);
        verify(m_attributeVisitor, atLeastOnce()).visit(any(OnmsAttribute.class));
    }

    @Test
    public void testVisitWithoutMatch() throws Exception {
        AttributeMatchingResourceVisitor resourceVisitor = new AttributeMatchingResourceVisitor();
        resourceVisitor.setAttributeVisitor(m_attributeVisitor);
        resourceVisitor.setAttributeMatch("ifInOctets");
        resourceVisitor.afterPropertiesSet();

        MockResourceType resourceType = new MockResourceType();
        resourceType.setName("something other than interfaceSnmp");
        OnmsResource resource = new OnmsResource("1", "Node One", resourceType, new HashSet<OnmsAttribute>(0), ResourcePath.get("foo"));

        doNothing().when(m_attributeVisitor).visit(any(OnmsAttribute.class));
        resourceVisitor.visit(resource);
        verify(m_attributeVisitor, never()).visit(any(OnmsAttribute.class));
    }
}
