/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.adapters.link;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.not;
import static org.easymock.EasyMock.or;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.EasyMockUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * Test the user stories/use cases associated with the Link Adapter.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath*:/META-INF/opennms/provisiond-extensions.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:testConfigContext.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class LinkProvisioningAdapterTest implements InitializingBean {
    
    public static final String END_POINT_1 = "nc-ral0001-to-ral0002-dwave";
    public static final String END_POINT_2 = "nc-ral0002-to-ral0001-dwave";
    public static final String UP_STATUS = "G";
    public static final String FAILED_STATUS = "B";

    @Autowired
    LinkProvisioningAdapter m_adapter;

    EasyMockUtils m_easyMock = new EasyMockUtils();

    private LinkMatchResolver m_matchResolver;

    private NodeLinkService m_nodeLinkService;
    
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
    
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
        
        EventBuilder eventBuilder = new EventBuilder(EventConstants.DATA_LINK_FAILED_EVENT_UEI, null);
        eventBuilder.setParam("endPoint1", END_POINT_1);
        eventBuilder.setParam("endPoint2", END_POINT_2);
        
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
        
        EventBuilder eventBuilder = new EventBuilder(EventConstants.DATA_LINK_FAILED_EVENT_UEI, null);
        eventBuilder.setParam("endPoint1", END_POINT_2);
        eventBuilder.setParam("endPoint2", END_POINT_1);
        eventBuilder.setCreationTime(new Date());
        eventBuilder.setDescription("nodeLinkFailed");
        
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
        
        EventBuilder eventBuilder = new EventBuilder(EventConstants.DATA_LINK_RESTORED_EVENT_UEI, null);
        eventBuilder.setParam("endPoint1", END_POINT_1);
        eventBuilder.setParam("endPoint2", END_POINT_2);
        eventBuilder.setCreationTime(new Date());
        eventBuilder.setDescription("nodeLinkFailed");
        
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
        
        EventBuilder eventBuilder = new EventBuilder(EventConstants.DATA_LINK_RESTORED_EVENT_UEI, null);
        eventBuilder.setParam("endPoint1", END_POINT_2);
        eventBuilder.setParam("endPoint2", END_POINT_1);
        eventBuilder.setCreationTime(new Date());
        eventBuilder.setDescription("nodeLinkFailed");
        
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


    /**
     * Test invocation of the addNode() operation to verify that the Spring context
     * is injecting all of the necessary dependencies.
     */
    @Test
    public void dwoAddNodeCallsDoAddNode() throws InterruptedException {
        m_adapter.addNode(1);
        Assert.assertEquals(1, m_adapter.getOperationQueue().getOperationQueueForNode(1).size());
        Thread.sleep(3000);
        Assert.assertEquals(0, m_adapter.getOperationQueue().getOperationQueueForNode(1).size());
    }

    @Test
    public void dwoDeleteNodeCallsDoDeleteNode() throws InterruptedException {
        m_adapter.deleteNode(1);
        Assert.assertEquals(1, m_adapter.getOperationQueue().getOperationQueueForNode(1).size());
        Thread.sleep(3000);
        Assert.assertEquals(0, m_adapter.getOperationQueue().getOperationQueueForNode(1).size());
    }

    @Test
    public void dwoUpdateNodeCallsDoUpdateNode() throws InterruptedException {
        m_adapter.updateNode(1);
        Assert.assertEquals(1, m_adapter.getOperationQueue().getOperationQueueForNode(1).size());
        Thread.sleep(3000);
        Assert.assertEquals(0, m_adapter.getOperationQueue().getOperationQueueForNode(1).size());
    }

    @Test
    public void dwoNotifyConfigChangeCallsDoNotifyConfigChange() throws InterruptedException {
        m_adapter.nodeConfigChanged(1);
        Assert.assertEquals(1, m_adapter.getOperationQueue().getOperationQueueForNode(1).size());
        Thread.sleep(3000);
        Assert.assertEquals(0, m_adapter.getOperationQueue().getOperationQueueForNode(1).size());
    }
}
