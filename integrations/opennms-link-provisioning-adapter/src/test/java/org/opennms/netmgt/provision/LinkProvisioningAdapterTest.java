/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: September 24, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.provision;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.not;
import static org.easymock.EasyMock.or;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.test.mock.EasyMockUtils;

/**
 * Test the user stories/use cases associated with the Link Adapter.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class LinkProvisioningAdapterTest {
    
    public static final String END_POINT_1 = "nc-ral0001-to-ral0002-dwave";
    public static final String END_POINT_2 = "nc-ral0002-to-ral0001-dwave";
    public static final String UP_STATUS = "G";
    public static final String FAILED_STATUS = "B";
    
    LinkProvisioningAdapter m_adapter;
    
    EasyMockUtils m_easyMock = new EasyMockUtils();

    private LinkMatchResolver m_matchResolver;

    private NodeLinkService m_nodeLinkService;
    
    
    @Before
    public void setUp() {
        m_matchResolver = createMock(LinkMatchResolver.class);
        m_nodeLinkService = createMock(NodeLinkService.class);
        
        expect(m_matchResolver.getAssociatedEndPoint(END_POINT_1)).andStubReturn(END_POINT_2);
        expect(m_matchResolver.getAssociatedEndPoint(END_POINT_2)).andStubReturn(END_POINT_1);
        expect(m_matchResolver.getAssociatedEndPoint(not(or(eq(END_POINT_1), eq(END_POINT_2))))).andStubReturn(null);
        
        expect(m_nodeLinkService.getNodeLabel(1)).andStubReturn(END_POINT_1);
        expect(m_nodeLinkService.getNodeLabel(2)).andStubReturn(END_POINT_2);
        expect(m_nodeLinkService.getNodeLabel(not(or(eq(1), eq(2))))).andStubReturn(null);
        
    }
    
    @Test
    public void dwoTestStubs(){
        replay();
        assertEquals(END_POINT_2, m_matchResolver.getAssociatedEndPoint(END_POINT_1));
        assertEquals(END_POINT_1, m_matchResolver.getAssociatedEndPoint(END_POINT_2));
        assertNull(m_matchResolver.getAssociatedEndPoint("other"));
        
        assertEquals(END_POINT_1, m_nodeLinkService.getNodeLabel(1));
        assertEquals(END_POINT_2, m_nodeLinkService.getNodeLabel(2));
        assertNull(m_nodeLinkService.getNodeLabel(17));
        
        verify();
    }
    
    
    
    @Test
    public void dwoAddLinkedNodes() {
        
        expect(m_nodeLinkService.getNodeId(END_POINT_1)).andStubReturn(1);
        
        // we make node2 return null the first time so when node1 is added it appear node2 is not there
        expect(m_nodeLinkService.getNodeId(END_POINT_2)).andReturn(null).andStubReturn(2);

        m_nodeLinkService.createLink(1, 2);
        
        replay();
        
        m_adapter = new LinkProvisioningAdapter();
        m_adapter.setLinkMatchResolver(m_matchResolver);
        m_adapter.setNodeLinkService(m_nodeLinkService);
        
        
        
        m_adapter.doAddNode(1);
        m_adapter.doAddNode(2);
         
        
        
        verify();
    }
    
    @Test
    public void dwoAddEndPoint2EndPoint1Exists() {
        
        expect(m_nodeLinkService.getNodeId(END_POINT_1)).andStubReturn(1);
        expect(m_nodeLinkService.getNodeId(END_POINT_2)).andStubReturn(2);

        m_nodeLinkService.createLink(1, 2);
        
        replay();
        
        m_adapter = new LinkProvisioningAdapter();
        m_adapter.setLinkMatchResolver(m_matchResolver);
        m_adapter.setNodeLinkService(m_nodeLinkService);
        
        m_adapter.doAddNode(2);
        
        verify();
    }
    
    @Test
    public void dwoAddEndPoint1EndPoint2Exists() {
        
        expect(m_nodeLinkService.getNodeId(END_POINT_1)).andStubReturn(1);
        expect(m_nodeLinkService.getNodeId(END_POINT_2)).andStubReturn(2);

        m_nodeLinkService.createLink(1, 2);
        
        replay();
        
        m_adapter = new LinkProvisioningAdapter();
        m_adapter.setLinkMatchResolver(m_matchResolver);
        m_adapter.setNodeLinkService(m_nodeLinkService);
        
        m_adapter.doAddNode(1);
        
        verify();
    }
    
    @Test
    public void dwoDataLinkFailedEventEndPoint1(){
        
        expect(m_nodeLinkService.getNodeId(END_POINT_1)).andStubReturn(1);
        expect(m_nodeLinkService.getNodeId(END_POINT_2)).andStubReturn(2);
        
        m_nodeLinkService.updateLinkStatus(1,2, FAILED_STATUS);
        
        replay();
        
        EventBuilder eventBuilder = new EventBuilder("uei.opennms.org/internal/linkd/dataLinkFailed", null);
        eventBuilder.setParam("endPoint1", END_POINT_1);
        eventBuilder.setParam("endPoint2", END_POINT_2);
        
        m_adapter = new LinkProvisioningAdapter();
        m_adapter.setLinkMatchResolver(m_matchResolver);
        m_adapter.setNodeLinkService(m_nodeLinkService);
        m_adapter.dataLinkFailed(eventBuilder.getEvent());
        
        verify();
    }
    
    @Test
    public void dwoDataLinkFailEventEndPoint2(){
        
        expect(m_nodeLinkService.getNodeId(END_POINT_1)).andStubReturn(1);
        expect(m_nodeLinkService.getNodeId(END_POINT_2)).andStubReturn(2);
        
        m_nodeLinkService.updateLinkStatus(1, 2, FAILED_STATUS);
        
        replay();
        
        EventBuilder eventBuilder = new EventBuilder("uei.opennms.org/internal/linkd/dataLinkFailed", null);
        eventBuilder.setParam("endPoint1", END_POINT_2);
        eventBuilder.setParam("endPoint2", END_POINT_1);
        eventBuilder.setCreationTime(new Date());
        eventBuilder.setDescription("nodeLinkFailed");
        
        m_adapter = new LinkProvisioningAdapter();
        m_adapter.setLinkMatchResolver(m_matchResolver);
        m_adapter.setNodeLinkService(m_nodeLinkService);
        m_adapter.dataLinkFailed(eventBuilder.getEvent());
        
        verify();
    }
    
    @Test
    public void dwoDataLinkRestoredEventEndPoint1(){

        expect(m_nodeLinkService.getNodeId(END_POINT_1)).andStubReturn(1);
        expect(m_nodeLinkService.getNodeId(END_POINT_2)).andStubReturn(2);
        
        m_nodeLinkService.updateLinkStatus(1,2, UP_STATUS);
        
        replay();
        
        EventBuilder eventBuilder = new EventBuilder("uei.opennms.org/internal/linkd/dataLinkRestored", null);
        eventBuilder.setParam("endPoint1", END_POINT_1);
        eventBuilder.setParam("endPoint2", END_POINT_2);
        eventBuilder.setCreationTime(new Date());
        eventBuilder.setDescription("nodeLinkFailed");
        
        m_adapter = new LinkProvisioningAdapter();
        m_adapter.setLinkMatchResolver(m_matchResolver);
        m_adapter.setNodeLinkService(m_nodeLinkService);
        m_adapter.dataLinkRestored(eventBuilder.getEvent());
        
        verify();
    }
    
    @Test
    public void dwoDataLinkRestoredEventEndPoint2(){

        expect(m_nodeLinkService.getNodeId(END_POINT_1)).andStubReturn(1);
        expect(m_nodeLinkService.getNodeId(END_POINT_2)).andStubReturn(2);
        
        m_nodeLinkService.updateLinkStatus(1,2, UP_STATUS);
        
        replay();
        
        EventBuilder eventBuilder = new EventBuilder("uei.opennms.org/internal/linkd/dataLinkRestored", null);
        eventBuilder.setParam("endPoint1", END_POINT_2);
        eventBuilder.setParam("endPoint2", END_POINT_1);
        eventBuilder.setCreationTime(new Date());
        eventBuilder.setDescription("nodeLinkFailed");
        
        m_adapter = new LinkProvisioningAdapter();
        m_adapter.setLinkMatchResolver(m_matchResolver);
        m_adapter.setNodeLinkService(m_nodeLinkService);
        m_adapter.dataLinkRestored(eventBuilder.getEvent());
        
        verify();
    }
    
    @Test
    public void dwoTestUpdateEndPoint1(){
        expect(m_nodeLinkService.getNodeId(END_POINT_1)).andStubReturn(1);
        
        // we make node2 return null the first time so when node1 is added it appear node2 is not there
        expect(m_nodeLinkService.getNodeId(END_POINT_2)).andReturn(null).andStubReturn(2);

        m_nodeLinkService.createLink(1, 2);
        
        replay();
        
        m_adapter = new LinkProvisioningAdapter();
        m_adapter.setLinkMatchResolver(m_matchResolver);
        m_adapter.setNodeLinkService(m_nodeLinkService);
        
        
        
        m_adapter.doAddNode(1);
        m_adapter.doAddNode(2);
         
        
        
        verify();
    }
    
    @Test
    public void dwoUpdateEndPoint2EndPoint1Exists() {
        
        expect(m_nodeLinkService.getNodeId(END_POINT_1)).andStubReturn(1);
        expect(m_nodeLinkService.getNodeId(END_POINT_2)).andStubReturn(2);

        m_nodeLinkService.createLink(1, 2);
        
        replay();
        
        m_adapter = new LinkProvisioningAdapter();
        m_adapter.setLinkMatchResolver(m_matchResolver);
        m_adapter.setNodeLinkService(m_nodeLinkService);
        
        m_adapter.doAddNode(2);
        
        verify();
    }
    
    @Test
    public void dwoUpdateEndPoint1EndPoint2Exists() {
        
        expect(m_nodeLinkService.getNodeId(END_POINT_1)).andStubReturn(1);
        expect(m_nodeLinkService.getNodeId(END_POINT_2)).andStubReturn(2);

        m_nodeLinkService.createLink(1, 2);
        
        replay();
        
        m_adapter = new LinkProvisioningAdapter();
        m_adapter.setLinkMatchResolver(m_matchResolver);
        m_adapter.setNodeLinkService(m_nodeLinkService);
        
        m_adapter.doAddNode(1);
        
        verify();
    }
    
    public <T> T createMock(Class<T> clazz){
        return m_easyMock.createMock(clazz);
    }
    
    public void verify(){
        m_easyMock.verifyAll();
    }
    
    public void replay(){
        m_easyMock.replayAll();
    }
}
