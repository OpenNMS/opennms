/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.junit.Test;
import org.opennms.netmgt.model.OnmsLinkState.LinkState;
import org.opennms.netmgt.model.OnmsLinkState.LinkStateTransition;
import org.opennms.test.mock.EasyMockUtils;


public class LinkStateTest {
    
    EasyMockUtils m_easyMockUtils = new EasyMockUtils();
    
    @Test
    public void testMoveFromBothUnmanagedToLinkNodeUnmanaged(){
        
       LinkState linkState = LinkState.LINK_BOTH_UNMANAGED;
       
       linkState = linkState.parentNodeEndPointFound(expectNoTransition());
       
       assertEquals(LinkState.LINK_NODE_UNMANAGED, linkState);
       verify();
    }
    
    @Test
    public void testMoveFromBothUnmanagedToParentUnmanaged(){
        
       LinkState linkState = LinkState.LINK_BOTH_UNMANAGED;
       
       linkState = linkState.nodeEndPointFound(expectNoTransition());
       
       assertEquals(LinkState.LINK_PARENT_NODE_UNMANAGED, linkState);
       verify();
    }
    
    @Test
    public void testMoveFromBothUnmanagedToLinkUp() {
        LinkState linkState = LinkState.LINK_BOTH_UNMANAGED;
        
        linkState = linkState.nodeEndPointFound(expectNoTransition());
        linkState = linkState.parentNodeEndPointFound(expectLinkUp());
        
        assertEquals(LinkState.LINK_UP, linkState);
    }
    
    @Test
    public void testMoveFromNodeUnmanagedToLinkUp() {
        LinkState linkState = LinkState.LINK_NODE_UNMANAGED;
        
        linkState = linkState.nodeEndPointFound(expectLinkUp());
        
        assertEquals(LinkState.LINK_UP, linkState);
        verify();
    }
    
    @Test
    public void testMoveFromLinkUpToNodeDown() {
        LinkState linkState = LinkState.LINK_UP;
        
        linkState = linkState.nodeDown(expectLinkDown());
        
        assertEquals(LinkState.LINK_NODE_DOWN, linkState);
        verify();
    }
    
    @Test
    public void testMoveFromNodeDownToBothDown() {
        LinkState linkState = LinkState.LINK_NODE_DOWN;
        
        linkState = linkState.parentNodeDown(expectNoTransition());
        
        assertEquals(LinkState.LINK_BOTH_DOWN, linkState);
        verify();
    }
    
    @Test
    public void testFromBothDownToParentNodeDown() {
        LinkState linkState = LinkState.LINK_BOTH_DOWN;
        
        linkState = linkState.nodeUp(expectNoTransition());
        
        assertEquals(LinkState.LINK_PARENT_NODE_DOWN, linkState);
        verify();
    }
    
    @Test
    public void testFromLinkUpToParentUnmanaged() {
        LinkState linkState = LinkState.LINK_UP;
        
        linkState = linkState.parentNodeEndPointDeleted(expectLinkUnknown());
        
        assertEquals(LinkState.LINK_PARENT_NODE_UNMANAGED, linkState);
        
        verify();
    }
    
    @Test
    public void testFromNodeDownToLinkUp() {
        LinkState linkState = LinkState.LINK_NODE_DOWN;
        
        linkState = linkState.nodeUp(expectLinkUp());
        
        assertEquals(LinkState.LINK_UP, linkState);
        verify();
    }
    
    @Test
    public void testFromParentNodeDownToLinkUp() {
        LinkState linkState = LinkState.LINK_PARENT_NODE_DOWN;
        
        linkState = linkState.parentNodeUp(expectLinkUp());
        
        assertEquals(LinkState.LINK_UP, linkState);
        verify();
    }
    
    @Test
    public void testFromBothDownToLinkUp() {
        LinkState linkState = LinkState.LINK_BOTH_DOWN;
        
        linkState = linkState.nodeUp(expectNoTransition());
        linkState = linkState.parentNodeUp(expectLinkUp());
        
        assertEquals(LinkState.LINK_UP, linkState);
    }
    
    public LinkStateTransition createEmptyTransition() {
        return new LinkStateTransition() {
            @Override
            public void onLinkUp() {
                
                
                
            }
            @Override
            public void onLinkUnknown() {}
            @Override
            public void onLinkDown() {}
        };
    }
    
    public void replay() {
        m_easyMockUtils.replayAll();
    }
    
    public void verify() {
        m_easyMockUtils.verifyAll();
    }
    
    private LinkStateTransition expectNoTransition() {
        LinkStateTransition transition = m_easyMockUtils.createMock(LinkStateTransition.class);
        
        EasyMock.replay(transition);
        
        return transition;
    }
    
    private LinkStateTransition expectLinkUp() {
        LinkStateTransition transition = m_easyMockUtils.createMock(LinkStateTransition.class);
        transition.onLinkUp();
        
        EasyMock.replay(transition);
        
        return transition;
    }
    
    private LinkStateTransition expectLinkDown() {
        LinkStateTransition transition = m_easyMockUtils.createMock(LinkStateTransition.class);
        transition.onLinkDown();
        
        EasyMock.replay(transition);
        
        return transition;
    }
    
    private LinkStateTransition expectLinkUnknown() {
        LinkStateTransition transition = m_easyMockUtils.createMock(LinkStateTransition.class);
        transition.onLinkUnknown();
        
        EasyMock.replay(transition);
        
        return transition;
    }
    

}
