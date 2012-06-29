/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

import java.util.Collections;
import java.util.HashSet;

import junit.framework.TestCase;

import org.opennms.netmgt.mock.MockResourceType;
import org.opennms.netmgt.model.AttributeVisitor;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.EasyMockUtils;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class AttributeMatchingResourceVisitorTest extends TestCase {
    private EasyMockUtils m_mocks = new EasyMockUtils();
    private AttributeVisitor m_attributeVisitor = m_mocks.createMock(AttributeVisitor.class);
    
    public void testAfterPropertiesSet() throws Exception {
        AttributeMatchingResourceVisitor resourceVisitor = new AttributeMatchingResourceVisitor();
        resourceVisitor.setAttributeVisitor(m_attributeVisitor);
        resourceVisitor.setAttributeMatch("ifInOctets");
        resourceVisitor.afterPropertiesSet();
    }


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

    public void testVisitWithMatch() throws Exception {
        AttributeMatchingResourceVisitor resourceVisitor = new AttributeMatchingResourceVisitor();
        resourceVisitor.setAttributeVisitor(m_attributeVisitor);
        resourceVisitor.setAttributeMatch("ifInOctets");
        resourceVisitor.afterPropertiesSet();

        MockResourceType resourceType = new MockResourceType();
        resourceType.setName("interfaceSnmp");
        OnmsAttribute attribute = new RrdGraphAttribute("ifInOctets", "something", "something else");
        OnmsResource resource = new OnmsResource("1", "Node One", resourceType, Collections.singleton(attribute));
        m_attributeVisitor.visit(attribute);

        m_mocks.replayAll();
        resourceVisitor.visit(resource);
        m_mocks.verifyAll();
    }
    
    public void testVisitWithoutMatch() throws Exception {
        AttributeMatchingResourceVisitor resourceVisitor = new AttributeMatchingResourceVisitor();
        resourceVisitor.setAttributeVisitor(m_attributeVisitor);
        resourceVisitor.setAttributeMatch("ifInOctets");
        resourceVisitor.afterPropertiesSet();

        MockResourceType resourceType = new MockResourceType();
        resourceType.setName("something other than interfaceSnmp");
        OnmsResource resource = new OnmsResource("1", "Node One", resourceType, new HashSet<OnmsAttribute>(0));

        m_mocks.replayAll();
        resourceVisitor.visit(resource);
        m_mocks.verifyAll();
    }
}
