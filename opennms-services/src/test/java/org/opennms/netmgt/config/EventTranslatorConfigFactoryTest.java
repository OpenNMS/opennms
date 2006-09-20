/*
 * Created on Nov 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.netmgt.config;

import java.io.Reader;
import java.io.StringReader;

import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockLogAppender;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.mock.OutageAnticipator;
import org.opennms.netmgt.translator.EventTranslator;

/**
 * @author mhuot
 * 
 */
public class EventTranslatorConfigFactoryTest extends OpenNMSTestCase {

	
    private EventTranslator m_translator;
    private MockEventIpcManager m_eventMgr;
    private String m_passiveStatusConfiguration = getStandardConfig();
    private EventTranslatorConfigFactory m_config;
    private EventAnticipator m_anticipator;
    private OutageAnticipator m_outageAnticipator;


    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        MockLogAppender.setupLogging();

        createAnticipators();

        m_eventMgr = new MockEventIpcManager();
        m_eventMgr.setEventWriter(m_db);
        m_eventMgr.setEventAnticipator(m_anticipator);
        m_eventMgr.addEventListener(m_outageAnticipator);
        m_eventMgr.setSynchronous(true);

        Reader rdr = new StringReader(m_passiveStatusConfiguration);
        m_config = new EventTranslatorConfigFactory(rdr, m_db);
        EventTranslatorConfigFactory.setInstance(m_config);

        m_translator = EventTranslator.getInstance();
        m_translator.setEventManager(m_eventMgr);
        m_translator.setConfig(EventTranslatorConfigFactory.getInstance());
        m_translator.setDataSource(m_db);
        
        m_translator.init();
        m_translator.start();

    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
		MockLogAppender.assertNoWarningsOrGreater();

    }
    
    public void testDoNothing() {
        // FIXME: This is because the below test is commented out
    }
    
    private void createAnticipators() {
        m_anticipator = new EventAnticipator();
        m_outageAnticipator = new OutageAnticipator(m_db);
    }

    private String getStandardConfig() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<event-translator-configuration \n" + 
        "xmlns=\"http://xmlns.opennms.org/xsd/translator-configuration\" \n" + 
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >\n" +
        "  <translation>\n" +
        "   <event-translation-spec uei=\"uei.opennms.org/services/translationTest\">\n" + 
        "      <mappings>\n" + 
        "        <mapping>\n" +
        "          <assignment type=\"field\" name=\"nodeid\">\n" +  
        "            <value type=\"sql\" result=\"select node.nodeid from node, ipInterface where node.nodeLabel=? and ipinterface.ipaddr=? and node.nodeId=ipinterface.nodeid and ipInterface.isManaged != 'D' and node.nodeType != 'D'\" >\n" +
        "				<value type=\"parameter\" name=\"passiveNodeLabel\" matches=\"Router\" result=\"Firewall\" />\n" +
        "				<value type=\"constant\" result=\"192.168.1.4\" />\n" +
        "			</value>\n" +
        "          </assignment>\n" + 
        "          <assignment type=\"parameter\" name=\"nodeLabel\">\n" +  
        "            <value type=\"field\" name=\"host\" result=\"Switch\" />\n" +
        "          </assignment>\n" + 
        "          <assignment type=\"field\" name=\"interface\">\n" + 
        "            <value type=\"parameter\" name=\"passiveIpAddr\" matches=\".*(192\\.168\\.1\\.1).*\" result=\"192.168.1.1\" />\n" +
        "          </assignment>\n" +
        "		  <assignment type=\"field\" name=\"host\">\n" +
        "			<value type=\"field\" name=\"host\" result=\"www.opennms.org\" />\n" +
        "		  </assignment>\n" + 
        "		  <assignment type=\"field\" name=\"descr\">\n" +
        "			<value type=\"constant\" result=\"a generated event\" />\n" +
        "		  </assignment>\n" + 
        "          <assignment type=\"field\" name=\"service\">\n" + 
        "            <value type=\"parameter\" name=\"passiveServiceName\" result=\"PSV\" />\n" + 
        "          </assignment>\n" + 
        "          <assignment type=\"parameter\" name=\"passiveStatus\">\n" + 
        "            <value type=\"parameter\" name=\"passiveStatus\" matches=\".*(Up|Down).*\" result=\"${1}\" />\n" + 
        "          </assignment>\n" + 
        "        </mapping>\n" + 
        "      </mappings>\n" + 
        "    </event-translation-spec>\n" + 
        "  </translation>\n" +
        "</event-translator-configuration>\n" + 
        "";
    }
}
