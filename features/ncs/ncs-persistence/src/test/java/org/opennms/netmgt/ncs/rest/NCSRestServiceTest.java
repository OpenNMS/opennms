package org.opennms.netmgt.ncs.rest;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.opennms.netmgt.ncs.persistence.NCSComponentService;
import org.opennms.test.mock.MockLogAppender;

public class NCSRestServiceTest extends AbstractSpringJerseyRestTestCase {
	@BeforeClass
	public static void setupLogging()
	{
		MockLogAppender.setupLogging(true, "DEBUG");
	}
	
	private static final String m_serviceXML = "" +
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
			"<component xmlns=\"http://xmlns.opennms.org/xsd/model/ncs\" type=\"Service\" foreignId=\"123\" foreignSource=\"NA-Service\">\n" + 
			"    <name>CokeP2P</name>\n" + 
			"    <component type=\"ServiceElement\" foreignId=\"8765,1234\" foreignSource=\"NA-ServiceElement\">\n" + 
			"        <name>PE1,ge-1/0/2</name>\n" + 
			"        <component type=\"ServiceElementComponent\" foreignId=\"8765,ge-1/0/2.50\" foreignSource=\"NA-SvcElemComp\">\n" + 
			"            <name>ge-1/0/2.50</name>\n" + 
			"            <component type=\"PhysicalInterface\" foreignId=\"8765,ifIndex-1\" foreignSource=\"NA-PhysIfs\">\n" + 
			"                <name>ge-1/0/2</name>\n" + 
			"            </component>\n" + 
			"        </component>\n" + 
			"        <component type=\"ServiceElementComponent\" foreignId=\"8765,vcid(50)\" foreignSource=\"NA-SvcElemComp\">\n" + 
			"            <name>PE1,vcid(50)</name>\n" + 
			"            <dependenciesRequired>ANY</dependenciesRequired>\n" + 
			"            <attributes>\n" + 
			"                <attribute>\n" + 
			"                    <key>jnxVpnPwVpnType</key>\n" + 
			"                    <value>5</value>\n" + 
			"                </attribute>\n" + 
			"                <attribute>\n" + 
			"                    <key>jnxVpnPwVpnName</key>\n" + 
			"                    <value>ge-1/0/2.2</value>\n" + 
			"                </attribute>\n" + 
			"            </attributes>\n" + 
			"            <component type=\"ServiceElementComponent\" foreignId=\"8765,LSP-1234\" foreignSource=\"NA-SvcElemComp\">\n" + 
			"                <name>lspA-PE1-PE2</name>\n" + 
			"            </component>\n" + 
			"            <component type=\"ServiceElementComponent\" foreignId=\"8765,LSP-4321\" foreignSource=\"NA-SvcElemComp\">\n" + 
			"                <name>lspB-PE1-PE2</name>\n" + 
			"            </component>\n" + 
			"        </component>\n" + 
			"    </component>\n" + 
			"    <component type=\"ServiceElement\" foreignId=\"9876,4321\" foreignSource=\"NA-ServiceElement\">\n" + 
			"        <name>PE2,ge-3/1/4</name>\n" + 
			"        <component type=\"ServiceElementComponent\" foreignId=\"9876,ge-3/1/4.50\" foreignSource=\"NA-SvcElemComp\">\n" + 
			"            <name>ge-3/1/4.50</name>\n" + 
			"            <component type=\"PhysicalInterface\" foreignId=\"9876,ifIndex-3\" foreignSource=\"NA-PhysIfs\">\n" + 
			"                <name>ge-3/1/4</name>\n" + 
			"            </component>\n" + 
			"        </component>\n" + 
			"        <component type=\"ServiceElementComponent\" foreignId=\"9876,vcid(50)\" foreignSource=\"NA-SvcElemComp\">\n" + 
			"            <name>PE2,vcid(50)</name>\n" + 
			"            <dependenciesRequired>ANY</dependenciesRequired>\n" + 
			"            <attributes>\n" + 
			"                <attribute>\n" + 
			"                    <key>jnxVpnPwVpnType</key>\n" + 
			"                    <value>5</value>\n" + 
			"                </attribute>\n" + 
			"                <attribute>\n" + 
			"                    <key>jnxVpnPwVpnName</key>\n" + 
			"                    <value>ge-3/1/4.2</value>\n" + 
			"                </attribute>\n" + 
			"            </attributes>\n" + 
			"            <component type=\"ServiceElementComponent\" foreignId=\"9876,LSP-1234\" foreignSource=\"NA-SvcElemComp\">\n" + 
			"                <name>lspA-PE2-PE1</name>\n" + 
			"            </component>\n" + 
			"            <component type=\"ServiceElementComponent\" foreignId=\"9876,LSP-4321\" foreignSource=\"NA-SvcElemComp\">\n" + 
			"                <name>lspB-PE2-PE1</name>\n" + 
			"            </component>\n" + 
			"        </component>\n" + 
			"    </component>\n" + 
			"</component>\n";

	private static final String[][] m_components = new String[][] {
		new String[] { "Service", "CokeP2P", "NA-Service", "123" },
		new String[] { "ServiceElement", "PE1,ge-1/0/2", "NA-ServiceElement", "8765,1234" },
		new String[] { "ServiceElementComponent", "ge-1/0/2.50", "NA-SvcElemComp", "8765,ge-1/0/2.50" },
		new String[] { "PhysicalInterface", "ge-1/0/2", "NA-PhysIfs", "8765,ifIndex-1" },
		new String[] { "ServiceElementComponent", "PE1,vcid(50)", "NA-SvcElemComp", "8765,vcid(50)" },
		new String[] { "ServiceElementComponent", "lspA-PE1-PE2", "NA-SvcElemComp", "8765,LSP-1234" },
		new String[] { "ServiceElementComponent", "lspB-PE1-PE2", "NA-SvcElemComp", "8765,LSP-4321" },
		new String[] { "ServiceElement", "PE2,ge-3/1/4", "NA-ServiceElement", "9876,4321" },
		new String[] { "ServiceElementComponent", "ge-3/1/4.50", "NA-SvcElemComp", "9876,ge-3/1/4.50" },
		new String[] { "PhysicalInterface", "ge-3/1/4", "NA-PhysIfs", "9876,ifIndex-3" },
		new String[] { "ServiceElementComponent", "PE2,vcid(50)", "NA-SvcElemComp", "9876,vcid(50)" },
		new String[] { "ServiceElementComponent", "lspA-PE2-PE1", "NA-SvcElemComp", "9876,LSP-1234" },
		new String[] { "ServiceElementComponent", "lspB-PE2-PE1", "NA-SvcElemComp", "9876,LSP-4321" }
	};

	private static final String m_extraXML = "" +
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
			"<component xmlns=\"http://xmlns.opennms.org/xsd/model/ncs\" type=\"ServiceElementComponent\" foreignId=\"monkey1\" foreignSource=\"NA-SvcElemComp\">\n" + 
			"    <name>Monkey (1)</name>\n" + 
			"    <component type=\"PhysicalInterface\" foreignId=\"shoe2\" foreignSource=\"NA-PhysIfs\">\n" + 
			"        <name>Shoe (2)</name>\n" + 
			"    </component>\n" + 
			"</component>\n";
	
	private static final String m_badForeignSourceXML = "" +
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
			"<component xmlns=\"http://xmlns.opennms.org/xsd/model/ncs\" type=\"Service\" foreignId=\"123\" foreignSource=\"NA-Service:1\">\n" + 
			"    <name>Blah</name>\n" + 
			"</component>\n";

	private static final String m_badForeignIdXML = "" +
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
			"<component xmlns=\"http://xmlns.opennms.org/xsd/model/ncs\" type=\"Service\" foreignId=\"123:456\" foreignSource=\"NA-Service\">\n" + 
			"    <name>Blah</name>\n" + 
			"</component>\n";

	private MockEventIpcManager m_eventIpcManager;
	private EventAnticipator m_eventAnticipator;

	@Override
	protected void afterServletStart() {
		m_eventIpcManager = getWebAppContext().getBean(MockEventIpcManager.class);
		m_eventAnticipator = m_eventIpcManager.getEventAnticipator();
		final NCSComponentService service = getWebAppContext().getBean(NCSComponentService.class);
		service.setEventIpcManager(m_eventIpcManager);
	}

	@After
	public void tearDown() {
		m_eventAnticipator.verifyAnticipated();
		m_eventAnticipator.reset();
	}
	
	@Test
	public void testPostAService() throws Exception {
		anticipateEvents(EventConstants.COMPONENT_ADDED_UEI);

		sendPost("/NCS", m_serviceXML);

		final NCSComponentRepository repo = getBean("ncsComponentRepository", NCSComponentRepository.class);
		for (final NCSComponent component : repo.findAll()) {
			LogUtils.debugf(this, "Found Component: %s/%s/%s", component.getType(), component.getForeignSource(), component.getForeignId());
		}
		String url = "/NCS/ServiceElementComponent/NA-SvcElemComp:9876%2Cvcid(50)";		
		// Testing GET Collection
		System.err.println("GET!!!");
		String xml = sendRequest(GET, url, 200);
		
		assertTrue(xml.contains("jnxVpnPwVpnName"));
	}

	@Test
	public void testDeleteAComponent() throws Exception {
		sendPost("/NCS", m_serviceXML);
		
		m_eventAnticipator.reset();

		anticipateEvent(EventConstants.COMPONENT_DELETED_UEI, new String[] { "ServiceElementComponent", "PE2,vcid(50)", "NA-SvcElemComp", "9876,vcid(50)" });

		String url = "/NCS/ServiceElementComponent/NA-SvcElemComp:9876%2Cvcid(50)";		
		// Testing GET Collection
		String xml = sendRequest(GET, url, 200);
		
		assertTrue(xml.contains("jnxVpnPwVpnName"));
		
		sendRequest(DELETE, url, 200);
		
		sendRequest(GET, url, 400);
		
		sendRequest(GET, "/NCS/Service/NA-Service:123", 200);
		
	}
	
	@Test
	public void testGetANonExistingService() throws Exception {
		
		// This service should not exist
		String url = "/NCS/Service/hello:world";

		// Testing GET Collection
		sendRequest(GET, url, 400);
		
		//assertTrue(xml.contains("Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0"));

	}
	
	@Test
	public void testFindAServiceByAttribute() throws Exception {
		
		sendPost("/NCS", m_serviceXML);
		
		m_eventAnticipator.reset();

		String url = "/NCS/attributes";
		// Testing GET Collection
		String xml = sendRequest(GET, url, 200);
		
		assertTrue(xml.contains("jnxVpnPwVpnName"));
	}

	/*
	 * <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
	 * <component xmlns="http://xmlns.opennms.org/xsd/model/ncs" type="ServiceElementComponent" foreignId="monkey1" foreignSource="NA-SvcElemComp">
	 *   <name>Monkey (1)</name>
	 *   <component type="PhysicalInterface" foreignId="shoe2" foreignSource="NA-PhysIfs">
	 *     <name>Shoe (2)</name>
	 *   </component>
	 * </component>
	 */
	@Test
	public void testAddComponents() throws Exception {
		sendPost("/NCS", m_serviceXML);

		m_eventAnticipator.reset();
		anticipateEvent(EventConstants.COMPONENT_UPDATED_UEI, new String[] { "ServiceElement", "PE2,ge-3/1/4", "NA-ServiceElement", "9876,4321" });
		anticipateEvent(EventConstants.COMPONENT_ADDED_UEI, new String[] { "ServiceElementComponent", "Monkey (1)", "NA-SvcElemComp", "monkey1" });
		anticipateEvent(EventConstants.COMPONENT_ADDED_UEI, new String[] { "PhysicalInterface", "Shoe (2)", "NA-PhysIfs", "shoe2" });

		String url = "/NCS/ServiceElement/NA-ServiceElement:9876,4321";
		
		sendPost(url, m_extraXML);

		String xml = sendRequest(GET, url, 200);
		assertTrue(xml.contains("monkey1"));
		
	}
	
	@Test
	@Ignore("allowing this for now")
	public void testInvalidForeignSource() throws Exception {
		sendPost("/NCS", m_badForeignSourceXML, 400);
	}

	@Test
	@Ignore("allowing this for now")
	public void testInvalidForeignId() throws Exception {
		sendPost("/NCS", m_badForeignIdXML, 400);
	}

	private void anticipateEvents(final String uei) {
		for (final String[] componentInfo : m_components) {
			anticipateEvent(uei, componentInfo);
		}
	}

	private void anticipateEvent(final String uei, final String[] componentInfo) {
		final EventBuilder builder = new EventBuilder(uei, "NCSComponentService");
		builder.addParam("componentType", componentInfo[0]);
		builder.addParam("componentName", componentInfo[1]);
		builder.addParam("componentForeignSource", componentInfo[2]);
		builder.addParam("componentForeignId", componentInfo[3]);
		m_eventAnticipator.anticipateEvent(builder.getEvent());
	}
}
