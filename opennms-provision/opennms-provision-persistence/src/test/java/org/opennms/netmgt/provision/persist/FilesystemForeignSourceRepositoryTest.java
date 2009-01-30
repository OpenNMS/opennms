package org.opennms.netmgt.provision.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.config.modelimport.ModelImport;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.provision.persist.requisition.OnmsRequisition;
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
        OnmsRequisition r = m_foreignSourceRepository.importRequisition(new ClassPathResource("/requisition-test.xml"));
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
        r.visit(v);
        assertEquals("number of nodes visited", 1, v.getNodeReqs().size());
        assertEquals("node name matches", "apknd", v.getNodeReqs().get(0).getNodeLabel());
    }

    @Test
    public void testForeignSource() throws Exception {
        createRequisition();
        OnmsForeignSource foreignSource = createForeignSource(m_defaultForeignSourceName);
        Set<OnmsForeignSource> foreignSources = m_foreignSourceRepository.getForeignSources();
        assertEquals("number of foreign sources must be 1", 1, foreignSources.size());
        assertEquals("getAll() foreign source name must match", m_defaultForeignSourceName, foreignSources.iterator().next().getName());
        assertEquals("get() must return the foreign source", foreignSource, m_foreignSourceRepository.getForeignSource(m_defaultForeignSourceName));
    }

    @Test
    public void testGetRequisition() throws Exception {
        OnmsRequisition requisition = createRequisition();
        OnmsForeignSource foreignSource = createForeignSource(m_defaultForeignSourceName);
        assertEquals("requisitions must match", m_foreignSourceRepository.getRequisition(m_defaultForeignSourceName), m_foreignSourceRepository.getRequisition(foreignSource));
        assertEquals("foreign source is the expected one", requisition, m_foreignSourceRepository.getRequisition(foreignSource));
    }

    @Test
    public void testDefaultForeignSource() throws Exception {
        createRequisition();
        List<String> detectorList = Arrays.asList(new String[]{ "Citrix", "DHCP", "DNS", "DominoIIOP", "FTP", "HTTP", "HTTPS", "ICMP",
                "IMAP", "LDAP", "NRPE", "POP3", "Radius", "SMB", "SMTP", "SNMP", "SSH" });
        String uuid = UUID.randomUUID().toString();
        OnmsForeignSource defaultForeignSource = m_foreignSourceRepository.getForeignSource(uuid);
        assertEquals("name must match requested foreign source repository name", uuid, defaultForeignSource.getName());
        assertEquals("scan-interval must be 1 day", 86400000, defaultForeignSource.getScanInterval().getMillis());
        assertEquals("foreign source must have no default policies", 0, defaultForeignSource.getPolicies().size());
        List<String> fsNames = new ArrayList<String>();
        for (PluginConfig config : defaultForeignSource.getDetectors()) {
            fsNames.add(config.getName());
        }
        assertEquals("detector list must match expected defaults", detectorList, fsNames);
        assertTrue("foreign source must be tagged as default", defaultForeignSource.isDefault());
    }
}