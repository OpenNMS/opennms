package org.opennms.netmgt.provision.persist;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.config.modelimport.Asset;
import org.opennms.netmgt.config.modelimport.Category;
import org.opennms.netmgt.config.modelimport.Interface;
import org.opennms.netmgt.config.modelimport.ModelImport;
import org.opennms.netmgt.config.modelimport.MonitoredService;
import org.opennms.netmgt.config.modelimport.Node;
import org.springframework.core.io.ClassPathResource;


public class MockForeignSourceRepositoryTest {
    private MockForeignSourceRepository m_repository;

    @Before
    public void setUp() {
        m_repository = new MockForeignSourceRepository();
    }
    
    public class TestVisitor implements ImportVisitor {
        private final List<Node> m_nodes = new ArrayList<Node>();
        
        public void completeAsset(Asset asset) {
        }

        public void completeCategory(Category category) {
        }

        public void completeInterface(Interface iface) {
        }

        public void completeModelImport(ModelImport modelImport) {
        }

        public void completeMonitoredService(MonitoredService svc) {
        }

        public void completeNode(Node node) {
            m_nodes.add(node);
        }

        public void visitAsset(Asset asset) {
        }

        public void visitCategory(Category category) {
        }

        public void visitInterface(Interface iface) {
        }

        public void visitModelImport(ModelImport mi) {
        }

        public void visitMonitoredService(MonitoredService svc) {
        }

        public void visitNode(Node node) {
        }
        
        public List<Node> getNodes() {
            return m_nodes;
        }
    }
    
    @Test
    public void testRequisition() {
        OnmsRequisition r = m_repository.createRequisition(new ClassPathResource("/requisition-test.xml"));
        m_repository.save(r);
        r = m_repository.getRequisition("imported:");
        TestVisitor v = new TestVisitor();
        r.visitImport(v);
        assertEquals("number of nodes visited", 1, v.getNodes().size());
        assertEquals("node name matches", "apknd", v.getNodes().get(0).getNodeLabel());
    }

    @Test
    public void testForeignSource() {
        OnmsRequisition r = m_repository.createRequisition(new ClassPathResource("/requisition-test.xml"));
        m_repository.save(r);
        Collection<OnmsForeignSource> fs = m_repository.getAll();
        System.err.println(fs);
    }

    /*
    @Test
    public void testGet() {
        Assert.notNull(foreignSourceName);
        return m_foreignSources.get(foreignSourceName);
    }

    @Test
    public void testGetAll() {
        return m_foreignSources.values();
    }
    
    @Test
    public void testGetRequisition() {
        String foreignSourceName;
        OnmsForeignSource foreignSource;
        m_requisitions.get(foreignSourceName);
        m_requisitions.get(foreignSource);
    }

    @Test
    public void testSave(OnmsForeignSource foreignSource) {
        String foreignSourceName;
        OnmsForeignSource foreignSource;
        save(foreignSourceName);
        save(foreignSource);
    }
    */
}
