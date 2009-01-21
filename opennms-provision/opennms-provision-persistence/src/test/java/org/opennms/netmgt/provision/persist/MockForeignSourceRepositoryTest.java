package org.opennms.netmgt.provision.persist;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.config.modelimport.ModelImport;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class
})
public class MockForeignSourceRepositoryTest {
    private String m_defaultForeignSourceName;

    private ForeignSourceRepository m_repository;

    @Before
    public void setUp() {
        m_repository = new MockForeignSourceRepository();
        ModelImport mi = new ModelImport();
        m_defaultForeignSourceName = mi.getForeignSource();
    }
    
    private OnmsRequisition createRequisition() throws Exception {
        OnmsRequisition r = m_repository.loadRequisition(new ClassPathResource("/requisition-test.xml"));
        m_repository.save(r);
        return r;
    }

    private OnmsForeignSource createForeignSource(String foreignSource) throws Exception {
        OnmsForeignSource fs = new OnmsForeignSource(foreignSource);
        fs.addDetector(new PluginConfig("HTTP", "org.opennms.netmgt.provision.detector.simple.HttpDetector"));
        fs.addPolicy(new PluginConfig("all-ipinterfaces", "org.opennms.netmgt.provision.persist.policies.InclusiveInterfacePolicy"));
        m_repository.save(fs);
        return fs;
    }

    @Test
    public void testRequisition() throws Exception {
        createRequisition();
        OnmsRequisition r = m_repository.getRequisition(m_defaultForeignSourceName);
        TestVisitor v = new TestVisitor();
        r.visitImport(v);
        assertEquals("number of nodes visited", 1, v.getNodes().size());
        assertEquals("node name matches", "apknd", v.getNodes().get(0).getNodeLabel());
    }

    @Test
    public void testForeignSource() throws Exception {
        createRequisition();
        OnmsForeignSource foreignSource = createForeignSource(m_defaultForeignSourceName);
        List<OnmsForeignSource> foreignSources = new ArrayList<OnmsForeignSource>(m_repository.getForeignSources());
        assertEquals("number of foreign sources", 1, foreignSources.size());
        assertEquals("getAll() foreign source name matches", m_defaultForeignSourceName, foreignSources.get(0).getName());
        assertEquals("get() returns the foreign source", foreignSource, m_repository.getForeignSource(m_defaultForeignSourceName));
    }

    @Test
    public void testGetRequisition() throws Exception {
        OnmsRequisition requisition = createRequisition();
        OnmsForeignSource foreignSource = createForeignSource(m_defaultForeignSourceName);
        assertEquals("foreign sources match", m_repository.getRequisition(m_defaultForeignSourceName), m_repository.getRequisition(foreignSource));
        assertEquals("foreign source is expected one", requisition, m_repository.getRequisition(foreignSource));
    }

}
