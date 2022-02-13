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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Test;
import org.opennms.netmgt.mock.MockResourceType;
import org.opennms.netmgt.model.ExternalValueAttribute;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceVisitor;
import org.opennms.netmgt.model.StringPropertyAttribute;
import org.opennms.test.ThrowableAnticipator;

/**
 * @author <a href="dj@opennms.org">DJ Gregor</a>
 */
public class ResourceAttributeFilteringResourceVisitorTest {
    private ResourceVisitor m_delegatedVisitor = mock(ResourceVisitor.class);
    
    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(m_delegatedVisitor);
    }

    @Test
    public void testAfterPropertiesSet() throws Exception {
        ResourceAttributeFilteringResourceVisitor filteringVisitor = new ResourceAttributeFilteringResourceVisitor();
        filteringVisitor.setDelegatedVisitor(m_delegatedVisitor);
        filteringVisitor.setResourceAttributeKey("ifSpeed");
        filteringVisitor.setResourceAttributeValueMatch("100000000");
        filteringVisitor.afterPropertiesSet();
    }

    @Test
    public void testAfterPropertiesSetNoDelegatedVisitor() throws Exception {
        ResourceAttributeFilteringResourceVisitor filteringVisitor = new ResourceAttributeFilteringResourceVisitor();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property delegatedVisitor must be set to a non-null value"));
        
        filteringVisitor.setDelegatedVisitor(null);
        filteringVisitor.setResourceAttributeKey("ifSpeed");

        try {
            filteringVisitor.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testAfterPropertiesSetNoResourceTypeMatch() throws Exception {
        ResourceAttributeFilteringResourceVisitor filteringVisitor = new ResourceAttributeFilteringResourceVisitor();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property resourceAttributeKey must be set to a non-null value"));

        filteringVisitor.setDelegatedVisitor(m_delegatedVisitor);
        filteringVisitor.setResourceAttributeValueMatch("1000000000");

        try {
            filteringVisitor.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testAfterPropertiesSetNoResourceAttributeValueMatch() throws Exception {
        ResourceAttributeFilteringResourceVisitor filteringVisitor = new ResourceAttributeFilteringResourceVisitor();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property resourceAttributeValueMatch must be set to a non-null value"));

        filteringVisitor.setDelegatedVisitor(m_delegatedVisitor);
        filteringVisitor.setResourceAttributeKey("ifSpeed");

        try {
            filteringVisitor.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testVisitWithExternalValueMatch() throws Exception {
        ResourceAttributeFilteringResourceVisitor filteringVisitor = new ResourceAttributeFilteringResourceVisitor();
        filteringVisitor.setDelegatedVisitor(m_delegatedVisitor);
        filteringVisitor.setResourceAttributeKey("ifSpeed");
        filteringVisitor.setResourceAttributeValueMatch("1000000000");
        filteringVisitor.afterPropertiesSet();

        Set<OnmsAttribute> attributes = new HashSet<OnmsAttribute>(1);
        attributes.add(new ExternalValueAttribute("ifSpeed", "1000000000"));

        MockResourceType resourceType = new MockResourceType();
        resourceType.setName("interfaceSnmp");
        OnmsResource resource = new OnmsResource("1", "Node One", resourceType, attributes, ResourcePath.get("foo"));

        m_delegatedVisitor.visit(resource);
        filteringVisitor.visit(resource);

        verify(m_delegatedVisitor, times(2)).visit(resource);
    }

    @Test
    public void testVisitWithStringPropertyMatch() throws Exception {
        ResourceAttributeFilteringResourceVisitor filteringVisitor = new ResourceAttributeFilteringResourceVisitor();
        filteringVisitor.setDelegatedVisitor(m_delegatedVisitor);
        filteringVisitor.setResourceAttributeKey("ifSpeed");
        filteringVisitor.setResourceAttributeValueMatch("1000000000");
        filteringVisitor.afterPropertiesSet();

        Set<OnmsAttribute> attributes = new HashSet<OnmsAttribute>(1);
        attributes.add(new StringPropertyAttribute("ifSpeed", "1000000000"));

        MockResourceType resourceType = new MockResourceType();
        resourceType.setName("interfaceSnmp");
        OnmsResource resource = new OnmsResource("1", "Node One", resourceType, attributes, ResourcePath.get("foo"));
        
        m_delegatedVisitor.visit(resource);
        filteringVisitor.visit(resource);

        verify(m_delegatedVisitor, times(2)).visit(resource);
    }

    @Test
    public void testVisitWithoutMatch() throws Exception {
        ResourceAttributeFilteringResourceVisitor filteringVisitor = new ResourceAttributeFilteringResourceVisitor();
        filteringVisitor.setDelegatedVisitor(m_delegatedVisitor);
        filteringVisitor.setResourceAttributeKey("ifSpeed");
        filteringVisitor.setResourceAttributeValueMatch("1000000000");
        filteringVisitor.afterPropertiesSet();

        MockResourceType resourceType = new MockResourceType();
        resourceType.setName("something other than interfaceSnmp");
        OnmsResource resource = new OnmsResource("1", "Node One", resourceType, new HashSet<OnmsAttribute>(0), ResourcePath.get("foo"));

        filteringVisitor.visit(resource);
    }
}
