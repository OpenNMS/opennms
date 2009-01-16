package org.opennms.netmgt.provision.persist;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.config.modelimport.ModelImport;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;


@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    DependencyInjectionTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:/testForeignSourceContext.xml"
})
public class FilesystemForeignSourceRepositoryTest {
    private String m_defaultForeignSourceName;

    @Autowired
    private ForeignSourceRepository m_foreignSourceRepository;

    @Autowired
    private ModelImport m_modelImport;
    
    @Before
    public void setUp() {
        m_defaultForeignSourceName = m_modelImport.getForeignSource();
    }

    private OnmsRequisition createRequisition() throws Exception {
        OnmsRequisition r = m_foreignSourceRepository.createRequisition(new ClassPathResource("/requisition-test.xml"));
        m_foreignSourceRepository.save(r);
        return r;
    }

    private OnmsForeignSource createForeignSource(String foreignSource) throws Exception {
        OnmsForeignSource fs = new OnmsForeignSource(foreignSource);
        fs.addDetector(new PluginConfig("HTTP", "org.opennms.netmgt.provision.detector.simple.HttpDetector"));
        fs.addPolicy(new PluginConfig("all-ipinterfaces", "org.opennms.netmgt.provision.persist.policies.InclusiveInterfacePolicy"));
        m_foreignSourceRepository.save(fs);
        return fs;
    }

    @Test
    public void testRequisition() throws Exception {
        createRequisition();
        OnmsRequisition r = m_foreignSourceRepository.getRequisition(m_defaultForeignSourceName);
        TestVisitor v = new TestVisitor();
        r.visitImport(v);
        assertEquals("number of nodes visited", 1, v.getNodes().size());
        assertEquals("node name matches", "apknd", v.getNodes().get(0).getNodeLabel());
    }

    @Test
    public void testForeignSource() throws Exception {
        createRequisition();
        OnmsForeignSource foreignSource = createForeignSource(m_defaultForeignSourceName);
        List<OnmsForeignSource> foreignSources = new ArrayList<OnmsForeignSource>(m_foreignSourceRepository.getAll());
        assertEquals("number of foreign sources", 1, foreignSources.size());
        assertEquals("getAll() foreign source name matches", m_defaultForeignSourceName, foreignSources.get(0).getName());
        assertEquals("get() returns the foreign source", foreignSource, m_foreignSourceRepository.get(m_defaultForeignSourceName));
    }

    @Test
    public void testGetRequisition() throws Exception {
        OnmsRequisition requisition = createRequisition();
        OnmsForeignSource foreignSource = createForeignSource(m_defaultForeignSourceName);
        assertEquals("requisitions match", m_foreignSourceRepository.getRequisition(m_defaultForeignSourceName), m_foreignSourceRepository.getRequisition(foreignSource));
        assertEquals("foreign source is expected one", requisition, m_foreignSourceRepository.getRequisition(foreignSource));
    }

}
