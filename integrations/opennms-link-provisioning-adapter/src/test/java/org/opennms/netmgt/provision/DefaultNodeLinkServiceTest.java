package org.opennms.netmgt.provision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.DataLinkInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;


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
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/provisiond-extensions.xml",
        "classpath:/linkTestContext.xml"
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
    DataLinkInterfaceDao m_dataLinkDao;    
    
    @Autowired
    JdbcTemplate m_jdbcTemplate;
    
    @Autowired
    NodeLinkService m_nodeLinkService;
    
    @Before
    public void setup(){
        m_dbPopulator.populateDatabase();
    }
    
    @Test
    public void dwoNotNull(){
        assertNotNull(m_dbPopulator);
        assertNotNull(m_nodeDao);
        assertNotNull(m_jdbcTemplate);
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
    
}
