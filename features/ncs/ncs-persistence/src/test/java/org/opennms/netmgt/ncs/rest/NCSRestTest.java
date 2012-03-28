package org.opennms.netmgt.ncs.rest;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.opennms.test.mock.MockLogAppender;

public class NCSRestTest extends AbstractSpringJerseyRestTestCase {
	
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

	@Test
	public void testPostAService() throws Exception {
		
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
		
		String url = "/NCS/attributes";
		// Testing GET Collection
		String xml = sendRequest(GET, url, 200);
		
		assertTrue(xml.contains("jnxVpnPwVpnName"));
	}

	@Test
	public void testAddComponents() throws Exception {
		sendPost("/NCS", m_serviceXML);

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
}
