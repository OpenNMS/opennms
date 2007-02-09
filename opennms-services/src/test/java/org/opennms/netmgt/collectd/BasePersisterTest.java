package org.opennms.netmgt.collectd;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.opennms.netmgt.config.DataCollectionConfig;
import org.opennms.netmgt.config.MibObject;
import org.opennms.netmgt.mock.MockDataCollectionConfig;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.snmp.TestSnmpValue;
import org.opennms.test.FileAnticipator;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;

import junit.framework.TestCase;

public class BasePersisterTest extends TestCase {
    
    private FileAnticipator m_fileAnticipator;
    private File m_snmpDirectory;
    private BasePersister m_persister;
    private OnmsIpInterface m_intf;
    private OnmsNode m_node;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        MockUtil.println("------------ Begin Test " + getName() + " --------------------------");
        MockLogAppender.setupLogging();

        m_fileAnticipator = new FileAnticipator();
        
        m_intf = new OnmsIpInterface();
        m_node = new OnmsNode();
        m_node.setId(1);
        m_intf.setNode(m_node);
    }
    
    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        MockLogAppender.assertNoWarningsOrGreater();
        m_fileAnticipator.deleteExpected();
    }
    
    @Override
    protected void tearDown() throws Exception {
        m_fileAnticipator.deleteExpected(true);
        m_fileAnticipator.tearDown();
        MockUtil.println("------------ End Test " + getName() + " --------------------------");
        super.tearDown();
    }
    
    public void testPersistStringAttributeWithExistingPropertiesFile() throws Exception {
        initPersister();
        
        File nodeDir = m_fileAnticipator.tempDir(getSnmpRrdDirectory(), m_node.getId().toString());
        m_fileAnticipator.tempFile(nodeDir, "strings.properties", "#just a test");
        
        Attribute attribute = buildStringAttribute();
        m_persister.persistStringAttribute(attribute);
    }
    
    public void testPersistStringAttributeWithParentDirectory() throws Exception {
        initPersister();
        
        File nodeDir = m_fileAnticipator.tempDir(getSnmpRrdDirectory(), m_node.getId().toString());
        m_fileAnticipator.expecting(nodeDir, "strings.properties");
        
        Attribute attribute = buildStringAttribute();
        m_persister.persistStringAttribute(attribute);
    }
    
    public void testPersistStringAttributeWithNoParentDirectory() throws Exception {
        initPersister();
        
        File nodeDir = m_fileAnticipator.expecting(getSnmpRrdDirectory(), m_node.getId().toString());
        m_fileAnticipator.expecting(nodeDir, "strings.properties");
        
        Attribute attribute = buildStringAttribute();
        m_persister.persistStringAttribute(attribute);
    }

    private Attribute buildStringAttribute() {
        CollectionAgent agent = new CollectionAgent(m_intf);
        DataCollectionConfig dataCollectionConfig = new MockDataCollectionConfig();
        OnmsSnmpCollection collection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, String>()), dataCollectionConfig);
        NodeResourceType resourceType = new NodeResourceType(agent, collection);
        CollectionResource resource = new NodeInfo(resourceType, agent);
        MibObject mibObject = new MibObject();
        mibObject.setOid(".1.1.1.1");
        mibObject.setAlias("mibObjectAlias");
        mibObject.setType("string");
        mibObject.setInstance("0");
        mibObject.setMaxval(null);
        mibObject.setMinval(null);
        AttributeType attributeType = new StringAttributeType(resourceType, "some-collection", mibObject, new AttributeGroupType("mibGroup", "ignore"));
        Attribute a = new Attribute(resource, attributeType, new TestSnmpValue.StringSnmpValue("foo"));
        return a;
    }

    private void initPersister() throws IOException {
        m_persister = new BasePersister();
        
        RrdRepository repository = new RrdRepository();
        repository.setRrdBaseDir(getSnmpRrdDirectory());
        m_persister.setRepository(repository);
    }

    private File getSnmpRrdDirectory() throws IOException {
        if (m_snmpDirectory == null) {
            m_snmpDirectory = m_fileAnticipator.tempDir("snmp"); 
        }
        return m_snmpDirectory;
    }
}
