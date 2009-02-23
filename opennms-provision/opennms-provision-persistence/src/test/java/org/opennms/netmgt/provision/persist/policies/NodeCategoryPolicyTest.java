package org.opennms.netmgt.provision.persist.policies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

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
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
})

@JUnitTemporaryDatabase()
public class NodeCategoryPolicyTest {
    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private DatabasePopulator m_populator;

    private List<OnmsNode> m_nodes;
    
    @Before
    public void setUp() {
        m_populator.populateDatabase();
        m_nodes = m_nodeDao.findAll();
    }
    
    @Test
    @Transactional
    public void testMatchingLabel() {
        NodeCategoryPolicy p = new NodeCategoryPolicy();
        p.setForeignId("1");
        p.setCategory("PolicyTest");

        List<OnmsNode> matchedNodes = matchPolicy(p, "1");
        assertTrue(matchedNodes.get(0).getCategories().contains(new OnmsCategory("PolicyTest")));
    }

    @Test
    @Transactional
    public void testMatchingNothing() {
        NodeCategoryPolicy p = new NodeCategoryPolicy();
        p.setLabel("~^wankerdoodle$");
        p.setCategory("PolicyTest");

        List<OnmsNode> matchedNodes = matchPolicy(p, null);
        assertEquals(0, matchedNodes.size());
    }

    private List<OnmsNode> matchPolicy(NodeCategoryPolicy p, String matchingId) {
        OnmsNode o;
        List<OnmsNode> populatedNodes = new ArrayList<OnmsNode>();
        List<OnmsNode> matchedNodes = new ArrayList<OnmsNode>();
        
        for (OnmsNode node : m_nodes) {
            System.err.println(node);
            o = p.apply(node);
            if (o != null) {
                matchedNodes.add(o);
            }
            if (node.getNodeId().equals(matchingId)) {
                populatedNodes.add(node);
            }
        }
        
        assertEquals(populatedNodes, matchedNodes);
        return matchedNodes;
    }

}
