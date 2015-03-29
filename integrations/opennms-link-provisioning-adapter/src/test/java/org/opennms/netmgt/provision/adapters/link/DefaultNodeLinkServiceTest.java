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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.LinkStateDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLinkState;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.OnmsLinkState.LinkState;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/provisiond-extensions.xml",
        "classpath:/testConfigContext.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class DefaultNodeLinkServiceTest implements InitializingBean {
    
    private int END_POINT1_ID;
    private int END_POINT2_ID;
    private int END_POINT3_ID;
    private String END_POINT1_LABEL = "node1";
    // private String END_POINT2_LABEL = "node2";
    // private String END_POINT3_LABEL = "node3";
    public String NO_SUCH_NODE_LABEL = "noSuchNode";
    
    @Autowired 
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    NodeDao m_nodeDao;
    
    @Autowired
    IpInterfaceDao m_ipInterfaceDao;
    
    @Autowired
    LinkStateDao m_linkStateDao;
    
    @Autowired
    DataLinkInterfaceDao m_dataLinkDao; 
    
    @Autowired
    MonitoredServiceDao m_monitoredServiceDao;
    
    @Autowired
    JdbcTemplate m_jdbcTemplate;
    
    @Autowired
    NodeLinkService m_nodeLinkService;
    
    @Autowired
    ServiceTypeDao m_serviceTypeDao;
    
    @Autowired
    TransactionTemplate m_transactionTemplate;
    
    @Before
    public void setup(){
        m_dbPopulator.populateDatabase();
        END_POINT1_ID = m_dbPopulator.getNode1().getId();
        END_POINT1_LABEL = m_dbPopulator.getNode1().getLabel();
        END_POINT2_ID = m_dbPopulator.getNode2().getId();
        END_POINT3_ID = m_dbPopulator.getNode3().getId();
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
    
    @Test
    @Transactional
    public void dwoTestGetNodeLabel(){
        String nodeLabel = m_nodeLinkService.getNodeLabel(END_POINT1_ID);
        
        assertNotNull(nodeLabel);
        assertEquals("node1", nodeLabel);
    }
    
    @Test
    @Transactional
    public void dwoTestNodeNotThere(){
        String nodeLabel = m_nodeLinkService.getNodeLabel(200);
        assertNull(nodeLabel);
    }
    
    @Test
    @Transactional
    public void dwoTestGetNodeId(){
        Integer nodeId = m_nodeLinkService.getNodeId(END_POINT1_LABEL);
        assertNotNull(nodeId);
        assertEquals(END_POINT1_ID, nodeId.intValue());
    }
    
    @Test
    @Transactional
    public void dwoTestGetNodeIdNull(){
        Integer nodeId = m_nodeLinkService.getNodeId(NO_SUCH_NODE_LABEL);
        assertNull(nodeId);
    }
    
    @Test
    @Transactional
    public void dwoTestCreateLink(){
        Collection<DataLinkInterface> dataLinks = m_dataLinkDao.findByNodeId(END_POINT3_ID);
        assertEquals(0, dataLinks.size());
        
        m_nodeLinkService.createLink(END_POINT1_ID, END_POINT3_ID);
        
        dataLinks = m_dataLinkDao.findByNodeId(END_POINT3_ID);
        assertEquals(1, dataLinks.size());
        
    }
    
    @Test
    @Transactional
    public void dwoTestLinkAlreadyExists(){
        Collection<DataLinkInterface> dataLinks = m_dataLinkDao.findByNodeId(END_POINT2_ID);
        assertEquals(1, dataLinks.size());
        
        m_nodeLinkService.createLink(END_POINT1_ID, END_POINT2_ID);
        
        dataLinks = m_dataLinkDao.findByNodeId(END_POINT2_ID);
        assertEquals(1, dataLinks.size());
    }
    
    @Test
    @Transactional
    public void dwoTestUpdateLinkStatus(){
        Collection<DataLinkInterface> dataLinks = m_dataLinkDao.findByNodeId(END_POINT2_ID);
        assertEquals(StatusType.ACTIVE, dataLinks.iterator().next().getStatus());
        int parentNodeId = END_POINT1_ID;
        int nodeId = END_POINT2_ID;
        
        m_nodeLinkService.updateLinkStatus(parentNodeId, nodeId, "G");
        
        dataLinks = m_dataLinkDao.findByNodeId(END_POINT2_ID);
        assertEquals(StatusType.GOOD, dataLinks.iterator().next().getStatus());
    }
    
    @Test
    @Transactional
    public void dwoTestUpdateLinkFailedStatus(){
        int parentNodeId = END_POINT1_ID;
        int nodeId = END_POINT2_ID;
        
        Collection<DataLinkInterface> dataLinks = m_dataLinkDao.findByNodeId(nodeId);
        assertEquals(StatusType.ACTIVE, dataLinks.iterator().next().getStatus());
        
        m_nodeLinkService.updateLinkStatus(parentNodeId, nodeId, "B");
        
        dataLinks = m_dataLinkDao.findByNodeId(nodeId);
        assertEquals(StatusType.BAD, dataLinks.iterator().next().getStatus());
    }
    
    @Test
    @Transactional
    public void dwoTestUpdateLinkGoodThenFailedStatus(){
        int parentNodeId = END_POINT1_ID;
        int nodeId = END_POINT2_ID;
        
        Collection<DataLinkInterface> dataLinks = m_dataLinkDao.findByNodeId(nodeId);
        assertEquals(StatusType.ACTIVE, dataLinks.iterator().next().getStatus());
        
        m_nodeLinkService.updateLinkStatus(parentNodeId, nodeId, "G");
        
        dataLinks = m_dataLinkDao.findByNodeId(nodeId);
        assertEquals(StatusType.GOOD, dataLinks.iterator().next().getStatus());
        
        m_nodeLinkService.updateLinkStatus(parentNodeId, nodeId, "B");
        
        dataLinks = m_dataLinkDao.findByNodeId(nodeId);
        assertEquals(StatusType.BAD, dataLinks.iterator().next().getStatus());
    }
    
    @Test
    @Transactional
    public void dwoTestGetLinkContainingNodeId() {
        int parentNodeId = END_POINT1_ID;
        
        Collection<DataLinkInterface> datalinks = m_nodeLinkService.getLinkContainingNodeId(parentNodeId);
        
        assertEquals(3, datalinks.size());
        
    }
    
    @Test
    @Transactional
    public void dwoTestGetLinkStateForInterface() {
        int nodeId = END_POINT2_ID;
        
        Collection<DataLinkInterface> dlis = m_nodeLinkService.getLinkContainingNodeId(nodeId);
        DataLinkInterface dli = dlis.iterator().next();
        assertNotNull(dli);
        
        OnmsLinkState linkState = new OnmsLinkState();
        linkState.setDataLinkInterface(dli);
        
        m_linkStateDao.save(linkState);
        m_linkStateDao.flush();
        
        linkState = m_nodeLinkService.getLinkStateForInterface(dli);
        
        assertNotNull("linkState was null", linkState);
        assertEquals(OnmsLinkState.LinkState.LINK_UP, linkState.getLinkState());
    }
    
    @Test
    @Transactional
    public void dwoTestSaveLinkState() {
        int nodeId = END_POINT2_ID;
        
        Collection<DataLinkInterface> dlis = m_nodeLinkService.getLinkContainingNodeId(nodeId);
        DataLinkInterface dli = dlis.iterator().next();
        
        OnmsLinkState linkState = new OnmsLinkState();
        linkState.setDataLinkInterface(dli);
        
        m_linkStateDao.save(linkState);
        m_linkStateDao.flush();
        
        OnmsLinkState linkState2 = m_nodeLinkService.getLinkStateForInterface(dli);
        
        assertNotNull("linkState was null", linkState2);
        assertEquals(OnmsLinkState.LinkState.LINK_UP, linkState2.getLinkState());
        
        linkState2.setLinkState(OnmsLinkState.LinkState.LINK_NODE_DOWN);
        
        m_nodeLinkService.saveLinkState(linkState2);
        
        OnmsLinkState linkState3 = m_nodeLinkService.getLinkStateForInterface(dli);
        
        assertEquals(OnmsLinkState.LinkState.LINK_NODE_DOWN, linkState3.getLinkState());
        
    }
    
    @Test
    @Transactional
    public void dwoTestSaveAllEnumStates() {
        int nodeId = END_POINT2_ID;
        
        Collection<DataLinkInterface> dlis = m_nodeLinkService.getLinkContainingNodeId(nodeId);
        DataLinkInterface dli = dlis.iterator().next();
        
        OnmsLinkState linkState = new OnmsLinkState();
        linkState.setDataLinkInterface(dli);
        
        
        for(LinkState ls : LinkState.values()){
            linkState.setLinkState(ls);
            saveLinkState(linkState);
        }
        
    }
    
    @Test
    @Transactional
    public void dwoTestAddPrimaryServiceToNode(){
        final String END_POINT_SERVICE_NAME = "EndPoint";
        addPrimaryServiceToNode(END_POINT1_ID, END_POINT_SERVICE_NAME);
        
        OnmsMonitoredService service = m_monitoredServiceDao.getPrimaryService(END_POINT1_ID, "ICMP");
        assertNotNull(service);
        assertEquals("ICMP", service.getServiceName());
        
        
        service = m_monitoredServiceDao.getPrimaryService(END_POINT1_ID, END_POINT_SERVICE_NAME);
        assertNotNull(service);
        assertEquals(END_POINT_SERVICE_NAME,service.getServiceName());
    }
    
    
    @Test
    @Transactional
    public void dwoTestNodeHasEndPointService() {
        
        assertFalse(m_nodeLinkService.nodeHasEndPointService(END_POINT1_ID));
        
        final String END_POINT_SERVICE_NAME = "EndPoint";
        addPrimaryServiceToNode(END_POINT1_ID, END_POINT_SERVICE_NAME);

        assertTrue(m_nodeLinkService.nodeHasEndPointService(END_POINT1_ID));
        
    }
    
    public void addPrimaryServiceToNode(final int nodeId, final String serviceName){
        m_transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                OnmsServiceType svcType = m_serviceTypeDao.findByName(serviceName);
                if(svcType == null){
                    svcType = new OnmsServiceType(serviceName);
                    m_serviceTypeDao.save(svcType);
                    m_serviceTypeDao.flush();
                }
                
                OnmsNode node = m_nodeDao.get(nodeId);
                
                OnmsIpInterface ipInterface = node.getPrimaryInterface();
                 
                OnmsMonitoredService svc = new OnmsMonitoredService();
                svc.setIpInterface(ipInterface);
                svc.setServiceType(svcType);
                m_monitoredServiceDao.save(svc);
                m_monitoredServiceDao.flush();
            }
        });
        
        
        
    }
    
    public void saveLinkState(final OnmsLinkState linkState){
        m_transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) {
                m_linkStateDao.saveOrUpdate(linkState);
                m_linkStateDao.flush();
            }
        });
    }
    
}
