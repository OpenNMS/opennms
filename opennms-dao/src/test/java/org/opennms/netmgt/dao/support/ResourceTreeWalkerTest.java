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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.mock.MockResourceType;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceVisitor;
import org.opennms.test.ThrowableAnticipator;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class ResourceTreeWalkerTest {
    private ResourceDao m_resourceDao = mock(ResourceDao.class);
    private ResourceVisitor m_visitor = mock(ResourceVisitor.class);

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(m_resourceDao);
        verifyNoMoreInteractions(m_visitor);
    }

    @Test
    public void testAfterPropertiesSet() {
        ResourceTreeWalker walker = new ResourceTreeWalker();
        walker.setResourceDao(m_resourceDao);
        walker.setVisitor(m_visitor);
        
        walker.afterPropertiesSet();
    }

    @Test
    public void testAfterPropertiesSetNoResourceDao() {
        ResourceTreeWalker walker = new ResourceTreeWalker();
        walker.setResourceDao(null);
        walker.setVisitor(m_visitor);
        
        ThrowableAnticipator ta = new  ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property resourceDao must be set to a non-null value"));
        
        try {
            walker.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testAfterPropertiesSetNoVisitor() {
        ResourceTreeWalker walker = new ResourceTreeWalker();
        walker.setResourceDao(m_resourceDao);
        walker.setVisitor(null);
        
        ThrowableAnticipator ta = new  ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property visitor must be set to a non-null value"));

        try {
            walker.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testWalkEmptyList() {
        ResourceTreeWalker walker = new ResourceTreeWalker();
        walker.setResourceDao(m_resourceDao);
        walker.setVisitor(m_visitor);
        
        walker.afterPropertiesSet();
        
        when(m_resourceDao.findTopLevelResources()).thenReturn(new ArrayList<OnmsResource>(0));

        walker.walk();

        verify(m_resourceDao, times(1)).findTopLevelResources();
    }

    @Test
    public void testWalkTopLevel() {
        ResourceTreeWalker walker = new ResourceTreeWalker();
        walker.setResourceDao(m_resourceDao);
        walker.setVisitor(m_visitor);
        
        walker.afterPropertiesSet();
        
        MockResourceType resourceType = new MockResourceType();
        List<OnmsResource> resources = new ArrayList<OnmsResource>(2);
        resources.add(new OnmsResource("1", "Node One", resourceType, new HashSet<OnmsAttribute>(0), new ResourcePath("foo")));
        resources.add(new OnmsResource("2", "Node Two", resourceType, new HashSet<OnmsAttribute>(0), new ResourcePath("foo")));

        when(m_resourceDao.findTopLevelResources()).thenReturn(resources);
        for (OnmsResource resource : resources) {
            m_visitor.visit(resource);
        }

        walker.walk();

        verify(m_resourceDao, times(1)).findTopLevelResources();
        verify(m_visitor, times(4)).visit(any(OnmsResource.class));
    }

    @Test
    public void testWalkChildren() {
        ResourceTreeWalker walker = new ResourceTreeWalker();
        walker.setResourceDao(m_resourceDao);
        walker.setVisitor(m_visitor);
        
        walker.afterPropertiesSet();
        
        MockResourceType resourceType = new MockResourceType();
        OnmsResource childResource = new OnmsResource("eth0", "Interface eth0", resourceType, new HashSet<OnmsAttribute>(0), new ResourcePath("foo"));
        OnmsResource topResource = new OnmsResource("1", "Node One", resourceType, new HashSet<OnmsAttribute>(0), Collections.singletonList(childResource), new ResourcePath("foo"));

        when(m_resourceDao.findTopLevelResources()).thenReturn(Collections.singletonList(topResource));

        m_visitor.visit(topResource);
        m_visitor.visit(childResource);

        walker.walk();

        verify(m_resourceDao, times(1)).findTopLevelResources();
        verify(m_visitor, times(4)).visit(any(OnmsResource.class));
    }
}
