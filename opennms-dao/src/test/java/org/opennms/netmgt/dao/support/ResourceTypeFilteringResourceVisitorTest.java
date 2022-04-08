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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashSet;

import org.junit.After;
import org.junit.Test;
import org.opennms.netmgt.mock.MockResourceType;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceVisitor;
import org.opennms.test.ThrowableAnticipator;

/**
 * @author <a href="dj@opennms.org">DJ Gregor</a>
 */
public class ResourceTypeFilteringResourceVisitorTest {
    private ResourceVisitor m_delegatedVisitor = mock(ResourceVisitor.class);

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(m_delegatedVisitor);
    }

    @Test
    public void testAfterPropertiesSet() throws Exception {
        ResourceTypeFilteringResourceVisitor filteringVisitor = new ResourceTypeFilteringResourceVisitor();
        filteringVisitor.setDelegatedVisitor(m_delegatedVisitor);
        filteringVisitor.setResourceTypeMatch("interfaceSnmp");
        filteringVisitor.afterPropertiesSet();
    }

    @Test
    public void testAfterPropertiesSetNoDelegatedVisitor() throws Exception {
        ResourceTypeFilteringResourceVisitor filteringVisitor = new ResourceTypeFilteringResourceVisitor();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property delegatedVisitor must be set to a non-null value"));
        
        filteringVisitor.setDelegatedVisitor(null);
        filteringVisitor.setResourceTypeMatch("interfaceSnmp");

        try {
            filteringVisitor.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testAfterPropertiesSetNoResourceTypeMatch() throws Exception {
        ResourceTypeFilteringResourceVisitor filteringVisitor = new ResourceTypeFilteringResourceVisitor();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property resourceTypeMatch must be set to a non-null value"));

        filteringVisitor.setDelegatedVisitor(m_delegatedVisitor);
        filteringVisitor.setResourceTypeMatch(null);

        try {
            filteringVisitor.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testVisitWithMatch() throws Exception {
        ResourceTypeFilteringResourceVisitor filteringVisitor = new ResourceTypeFilteringResourceVisitor();
        filteringVisitor.setDelegatedVisitor(m_delegatedVisitor);
        filteringVisitor.setResourceTypeMatch("interfaceSnmp");
        filteringVisitor.afterPropertiesSet();

        MockResourceType resourceType = new MockResourceType();
        resourceType.setName("interfaceSnmp");
        OnmsResource resource = new OnmsResource("1", "Node One", resourceType, new HashSet<OnmsAttribute>(0), new ResourcePath("foo"));
        m_delegatedVisitor.visit(resource);

        filteringVisitor.visit(resource);

        verify(m_delegatedVisitor, times(2)).visit(any(OnmsResource.class));
    }

    @Test
    public void testVisitWithoutMatch() throws Exception {
        ResourceTypeFilteringResourceVisitor filteringVisitor = new ResourceTypeFilteringResourceVisitor();
        filteringVisitor.setDelegatedVisitor(m_delegatedVisitor);
        filteringVisitor.setResourceTypeMatch("interfaceSnmp");
        filteringVisitor.afterPropertiesSet();

        MockResourceType resourceType = new MockResourceType();
        resourceType.setName("something other than interfaceSnmp");
        OnmsResource resource = new OnmsResource("1", "Node One", resourceType, new HashSet<OnmsAttribute>(0), new ResourcePath("foo"));

        filteringVisitor.visit(resource);
    }
}
