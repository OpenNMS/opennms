package org.opennms.netmgt.provision.adapters.link;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.LinkStateDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLinkState;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;


@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/provisiond-extensions.xml",
        "classpath:/testConfigContext.xml"
})
@JUnitTemporaryDatabase()
public class DefaultNodeLinkServiceTest {
    
    private static final int END_POINT1_ID = 1;
    private static final int END_POINT2_ID = 2;
    private static final int END_POINT3_ID = 3;
    private static final String END_POINT1_LABEL = "node1";
    private static final String END_POINT2_LABEL = "node2";
    private static final String END_POINT3_LABEL = "node3";
    public static final String NO_SUCH_NODE_LABEL = "noSuchNode";
    
    
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
    }
    
    @Test
    public void dwoNotNull(){
        assertNotNull(m_dbPopulator);
        assertNotNull(m_nodeDao);
        assertNotNull(m_jdbcTemplate);
        assertNotNull(m_monitoredServiceDao);
    }
    
    @Test
    public void dwoTestGetNodeLabel(){
        String nodeLabel = m_nodeLinkService.getNodeLabel(END_POINT1_ID);
        
        assertNotNull(nodeLabel);
        assertEquals("node1", nodeLabel);
    }
    
    @Test
    public void dwoTestNodeNotThere(){
        String nodeLabel = m_nodeLinkService.getNodeLabel(200);
        assertNull(nodeLabel);
    }
    
    @Test
    public void dwoTestGetNodeId(){
        Integer nodeId = m_nodeLinkService.getNodeId(END_POINT1_LABEL);
        assertNotNull(nodeId);
        assertEquals(Integer.valueOf(1), nodeId);
    }
    
    @Test
    public void dwoTestGetNodeIdNull(){
        Integer nodeId = m_nodeLinkService.getNodeId(NO_SUCH_NODE_LABEL);
        assertNull(nodeId);
    }
    
    @Test
    public void dwoTestCreateLink(){
        Collection<DataLinkInterface> dataLinks = m_dataLinkDao.findByNodeId(END_POINT3_ID);
        assertEquals(0, dataLinks.size());
        
        m_nodeLinkService.createLink(1, 3);
        
        dataLinks = m_dataLinkDao.findByNodeId(END_POINT3_ID);
        assertEquals(1, dataLinks.size());
        
    }
    
    @Test
    public void dwoTestLinkAlreadyExists(){
        Collection<DataLinkInterface> dataLinks = m_dataLinkDao.findByNodeId(END_POINT2_ID);
        assertEquals(1, dataLinks.size());
        
        m_nodeLinkService.createLink(1, 2);
        
        dataLinks = m_dataLinkDao.findByNodeId(END_POINT2_ID);
        assertEquals(1, dataLinks.size());
    }
    
    @Test
    public void dwoTestUpdateLinkStatus(){
        Collection<DataLinkInterface> dataLinks = m_dataLinkDao.findByNodeId(END_POINT2_ID);
        assertEquals("A", dataLinks.iterator().next().getStatus());
        int parentNodeId = END_POINT1_ID;
        int nodeId = END_POINT2_ID;
        
        m_nodeLinkService.updateLinkStatus(parentNodeId, nodeId, "G");
        
        dataLinks = m_dataLinkDao.findByNodeId(END_POINT2_ID);
        assertEquals("G", dataLinks.iterator().next().getStatus());
    }
    
    @Test
    public void dwoTestUpdateLinkFailedStatus(){
        int parentNodeId = END_POINT1_ID;
        int nodeId = END_POINT2_ID;
        
        Collection<DataLinkInterface> dataLinks = m_dataLinkDao.findByNodeId(nodeId);
        assertEquals("A", dataLinks.iterator().next().getStatus());
        
        m_nodeLinkService.updateLinkStatus(parentNodeId, nodeId, "B");
        
        dataLinks = m_dataLinkDao.findByNodeId(nodeId);
        assertEquals("B", dataLinks.iterator().next().getStatus());
    }
    
    @Test
    public void dwoTestUpdateLinkGoodThenFailedStatus(){
        int parentNodeId = END_POINT1_ID;
        int nodeId = END_POINT2_ID;
        
        Collection<DataLinkInterface> dataLinks = m_dataLinkDao.findByNodeId(nodeId);
        assertEquals("A", dataLinks.iterator().next().getStatus());
        
        m_nodeLinkService.updateLinkStatus(parentNodeId, nodeId, "G");
        
        dataLinks = m_dataLinkDao.findByNodeId(nodeId);
        assertEquals("G", dataLinks.iterator().next().getStatus());
        
        m_nodeLinkService.updateLinkStatus(parentNodeId, nodeId, "B");
        
        dataLinks = m_dataLinkDao.findByNodeId(nodeId);
        assertEquals("B", dataLinks.iterator().next().getStatus());
    }
    
    @Test
    public void dwoTestGetLinkContainingNodeId() {
        int parentNodeId = END_POINT1_ID;
        
        Collection<DataLinkInterface> datalinks = m_nodeLinkService.getLinkContainingNodeId(parentNodeId);
        
        assertEquals(3, datalinks.size());
        
    }
    
    @Test
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
        
        linkState2.setLinkState(linkState2.getLinkState().LINK_NODE_DOWN);
        
        m_nodeLinkService.saveLinkState(linkState2);
        
        OnmsLinkState linkState3 = m_nodeLinkService.getLinkStateForInterface(dli);
        
        assertEquals(OnmsLinkState.LinkState.LINK_NODE_DOWN, linkState3.getLinkState());
        
    }
    
    @Test
    public void dwoTestSaveLinkParentNodeUnmanagedState() {
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
        
        linkState2.setLinkState(linkState2.getLinkState().LINK_PARENT_NODE_UNMANAGED);
        
        m_nodeLinkService.saveLinkState(linkState2);
        
        OnmsLinkState linkState3 = m_nodeLinkService.getLinkStateForInterface(dli);
        
        assertEquals(OnmsLinkState.LinkState.LINK_PARENT_NODE_UNMANAGED, linkState3.getLinkState());
        
    }
    
    @Test
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
    public void dwoTestNodeHasEndPointService() {
        
        assertFalse(m_nodeLinkService.nodeHasEndPointService(END_POINT1_ID));
        
        final String END_POINT_SERVICE_NAME = "EndPoint";
        addPrimaryServiceToNode(END_POINT1_ID, END_POINT_SERVICE_NAME);

        assertTrue(m_nodeLinkService.nodeHasEndPointService(END_POINT1_ID));
        
    }
    
    public void addPrimaryServiceToNode(final int nodeId, final String serviceName){
        m_transactionTemplate.execute(new TransactionCallback() {
            
            public Object doInTransaction(TransactionStatus status) {
                OnmsServiceType svcType = m_serviceTypeDao.findByName(serviceName);
                if(svcType == null){
                    svcType = new OnmsServiceType(serviceName);
                    m_serviceTypeDao.save(svcType);
                }
                
                OnmsNode node = m_nodeDao.get(nodeId);
                
                OnmsIpInterface ipInterface = node.getPrimaryInterface();
                 
                OnmsMonitoredService svc = new OnmsMonitoredService();
                svc.setIpInterface(ipInterface);
                svc.setServiceType(svcType);
                m_monitoredServiceDao.save(svc);
                
                return null;
            }
        });
        
        
        
    }
    
}
