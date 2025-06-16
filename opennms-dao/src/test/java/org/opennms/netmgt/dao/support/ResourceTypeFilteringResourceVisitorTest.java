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
