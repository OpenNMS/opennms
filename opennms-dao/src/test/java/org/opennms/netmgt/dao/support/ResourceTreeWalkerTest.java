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

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestCase;

import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.mock.MockResourceType;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourceVisitor;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.EasyMockUtils;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class ResourceTreeWalkerTest extends TestCase {
    private EasyMockUtils m_mocks = new EasyMockUtils();
    private ResourceDao m_resourceDao = m_mocks.createMock(ResourceDao.class);
    private ResourceVisitor m_visitor = m_mocks.createMock(ResourceVisitor.class);
    
    public void setUp() throws Exception {
        super.setUp();
    }
    
    public void testAfterPropertiesSet() {
        ResourceTreeWalker walker = new ResourceTreeWalker();
        walker.setResourceDao(m_resourceDao);
        walker.setVisitor(m_visitor);
        
        m_mocks.replayAll();
        walker.afterPropertiesSet();
        m_mocks.verifyAll();
    }
    
    public void testAfterPropertiesSetNoResourceDao() {
        ResourceTreeWalker walker = new ResourceTreeWalker();
        walker.setResourceDao(null);
        walker.setVisitor(m_visitor);
        
        ThrowableAnticipator ta = new  ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property resourceDao must be set to a non-null value"));
        
        m_mocks.replayAll();
        try {
            walker.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
        m_mocks.verifyAll();
    }
    
    public void testAfterPropertiesSetNoVisitor() {
        ResourceTreeWalker walker = new ResourceTreeWalker();
        walker.setResourceDao(m_resourceDao);
        walker.setVisitor(null);
        
        ThrowableAnticipator ta = new  ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property visitor must be set to a non-null value"));

        m_mocks.replayAll();
        try {
            walker.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
        m_mocks.verifyAll();
    }
    
    public void testWalkEmptyList() {
        ResourceTreeWalker walker = new ResourceTreeWalker();
        walker.setResourceDao(m_resourceDao);
        walker.setVisitor(m_visitor);
        
        m_mocks.replayAll();
        walker.afterPropertiesSet();
        m_mocks.verifyAll();
        
        expect(m_resourceDao.findTopLevelResources()).andReturn(new ArrayList<OnmsResource>(0));

        m_mocks.replayAll();
        walker.walk();
        m_mocks.verifyAll();
    }
    
    public void testWalkTopLevel() {
        ResourceTreeWalker walker = new ResourceTreeWalker();
        walker.setResourceDao(m_resourceDao);
        walker.setVisitor(m_visitor);
        
        m_mocks.replayAll();
        walker.afterPropertiesSet();
        m_mocks.verifyAll();
        
        MockResourceType resourceType = new MockResourceType();
        List<OnmsResource> resources = new ArrayList<OnmsResource>(2);
        resources.add(new OnmsResource("1", "Node One", resourceType, new HashSet<OnmsAttribute>(0)));
        resources.add(new OnmsResource("2", "Node Two", resourceType, new HashSet<OnmsAttribute>(0)));
        expect(m_resourceDao.findTopLevelResources()).andReturn(resources);
        for (OnmsResource resource : resources) {
            m_visitor.visit(resource);
        }

        m_mocks.replayAll();
        walker.walk();
        m_mocks.verifyAll();
    }
    
    public void testWalkChildren() {
        ResourceTreeWalker walker = new ResourceTreeWalker();
        walker.setResourceDao(m_resourceDao);
        walker.setVisitor(m_visitor);
        
        m_mocks.replayAll();
        walker.afterPropertiesSet();
        m_mocks.verifyAll();
        
        MockResourceType resourceType = new MockResourceType();
        OnmsResource childResource = new OnmsResource("eth0", "Interface eth0", resourceType, new HashSet<OnmsAttribute>(0));
        OnmsResource topResource = new OnmsResource("1", "Node One", resourceType, new HashSet<OnmsAttribute>(0), Collections.singletonList(childResource));
        expect(m_resourceDao.findTopLevelResources()).andReturn(Collections.singletonList(topResource));
        m_visitor.visit(topResource);
        m_visitor.visit(childResource);

        m_mocks.replayAll();
        walker.walk();
        m_mocks.verifyAll();
    }
}
