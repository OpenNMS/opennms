package org.opennms.netmgt.ncs.rest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.mock.EventAnticipator;
import org.opennms.netmgt.eventd.mock.MockEventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.opennms.netmgt.ncs.persistence.NCSComponentDao;
import org.opennms.netmgt.ncs.persistence.NCSComponentService;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.springframework.mock.web.MockHttpServletRequest;

public class NCSRestServiceTest extends AbstractSpringJerseyRestTestCase {
	private static void setupLogging(final String level) {
		final Properties config = new Properties();
		config.setProperty("log4j.logger.org.opennms.netmgt.mock.MockEventIpcManager", "ERROR");
		config.setProperty("log4j.logger.org.springframework", "ERROR");
		config.setProperty("log4j.logger.org.hibernate", "ERROR");
		MockLogAppender.setupLogging(true, level, config);
	}

	@BeforeClass
	public static void setupLogging() {
		setupLogging("ERROR");
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
			new String[] { "Service",                 "CokeP2P",      "NA-Service",        "123" },
			new String[] { "ServiceElement",          "PE1,ge-1/0/2", "NA-ServiceElement", "8765,1234" },
			new String[] { "ServiceElementComponent", "ge-1/0/2.50",  "NA-SvcElemComp",    "8765,ge-1/0/2.50" },
			new String[] { "PhysicalInterface",       "ge-1/0/2",     "NA-PhysIfs",        "8765,ifIndex-1" },
			new String[] { "ServiceElementComponent", "PE1,vcid(50)", "NA-SvcElemComp",    "8765,vcid(50)" },
			new String[] { "ServiceElementComponent", "lspA-PE1-PE2", "NA-SvcElemComp",    "8765,LSP-1234" },
			new String[] { "ServiceElementComponent", "lspB-PE1-PE2", "NA-SvcElemComp",    "8765,LSP-4321" },
			new String[] { "ServiceElement",          "PE2,ge-3/1/4", "NA-ServiceElement", "9876,4321" },
			new String[] { "ServiceElementComponent", "ge-3/1/4.50",  "NA-SvcElemComp",    "9876,ge-3/1/4.50" },
			new String[] { "PhysicalInterface",       "ge-3/1/4",     "NA-PhysIfs",        "9876,ifIndex-3" },
			new String[] { "ServiceElementComponent", "PE2,vcid(50)", "NA-SvcElemComp",    "9876,vcid(50)" },
			new String[] { "ServiceElementComponent", "lspA-PE2-PE1", "NA-SvcElemComp",    "9876,LSP-1234" },
			new String[] { "ServiceElementComponent", "lspB-PE2-PE1", "NA-SvcElemComp",    "9876,LSP-4321" } };

	private static final String m_serviceXMLFragment = "" +
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
			"<component xmlns=\"http://xmlns.opennms.org/xsd/model/ncs\" type=\"ServiceElementComponent\" foreignId=\"9876,vcid(50)\" foreignSource=\"NA-SvcElemComp\">\n" +
			"  <name>PE2,vcid(50)</name>\n" +
			"  <dependenciesRequired>ANY</dependenciesRequired>\n" +
			"</component>\n";

	private static final String m_serviceXMLTopFragment = "" +
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
			"<component xmlns=\"http://xmlns.opennms.org/xsd/model/ncs\" type=\"Service\" foreignId=\"123\" foreignSource=\"NA-Service\">\n" +
			"    <name>CokeP2P</name>\n" +
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

	private MockEventIpcManager m_eventIpcManager;
	private EventAnticipator m_eventAnticipator;

	@Override
	protected void afterServletStart() throws Exception {
		m_eventIpcManager = getWebAppContext().getBean(MockEventIpcManager.class);
		m_eventAnticipator = m_eventIpcManager.getEventAnticipator();
		final NCSComponentService service = getWebAppContext().getBean(NCSComponentService.class);
		service.setEventProxy(m_eventIpcManager);
	}

	@After
	@Override
	public void tearDown() throws Exception {
		final Collection<Event> events = m_eventAnticipator.unanticipatedEvents();
		for (final Event e : events) {
			System.err.println("unanticipated event: " + e.getUei() + formatParms(e.getParmCollection()));
		}

		setupLogging("ERROR");
		m_eventAnticipator.verifyAnticipated();
		m_eventAnticipator.reset();

		final NCSComponentDao dao = getWebAppContext().getBean(NCSComponentDao.class);
		dao.flush();
		
		super.tearDown();
	}

	@Test
	public void testPostAService() throws Exception {
		setupLogging("DEBUG");

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

		anticipateEvent(EventConstants.COMPONENT_DELETED_UEI, new String[] { "ServiceElementComponent", "PE2,vcid(50)", "NA-SvcElemComp",    "9876,vcid(50)" });
		anticipateEvent(EventConstants.COMPONENT_UPDATED_UEI, new String[] { "ServiceElement",          "PE2,ge-3/1/4", "NA-ServiceElement", "9876,4321" });

		setupLogging("DEBUG");

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
		setupLogging("DEBUG");

		// This service should not exist
		String url = "/NCS/Service/hello:world";

		// Testing GET Collection
		sendRequest(GET, url, 400);

	}

	@Test
	public void testFindAServiceByAttribute() throws Exception {
		sendPost("/NCS", m_serviceXML);

		m_eventAnticipator.reset();

		setupLogging("DEBUG");

		String url = "/NCS/attributes";
		// Testing GET Collection
		String xml = sendRequest(GET, url, 200);

		assertTrue(xml.contains("jnxVpnPwVpnName"));
	}

	@Test
	public void testAddComponents() throws Exception {
		sendPost("/NCS", m_serviceXML);

		m_eventAnticipator.reset();
		anticipateEvent(EventConstants.COMPONENT_UPDATED_UEI, new String[] { "ServiceElement",          "PE2,ge-3/1/4", "NA-ServiceElement", "9876,4321" });
		anticipateEvent(EventConstants.COMPONENT_ADDED_UEI,   new String[] { "ServiceElementComponent", "Monkey (1)",   "NA-SvcElemComp",    "monkey1" });
		anticipateEvent(EventConstants.COMPONENT_ADDED_UEI,   new String[] { "PhysicalInterface",       "Shoe (2)",     "NA-PhysIfs",        "shoe2" });

		setupLogging("DEBUG");

		String url = "/NCS/ServiceElement/NA-ServiceElement:9876,4321";

		sendPost(url, m_extraXML);

		String xml = sendRequest(GET, url, 200);
		assertTrue(xml.contains("monkey1"));

	}

	@Test
	public void testDeleteOrphans() throws Exception {
		sendPost("/NCS", m_serviceXML);

		m_eventAnticipator.reset();
		anticipateEvent(EventConstants.COMPONENT_UPDATED_UEI, new String[] { "ServiceElementComponent", "PE2,vcid(50)", "NA-SvcElementComp", "9876,vcid(50)" });
		anticipateEvent(EventConstants.COMPONENT_DELETED_UEI, new String[] { "ServiceElementComponent", "lspA-PE2-PE1", "NA-SvcElemComp",    "9876,LSP-1234" });
		anticipateEvent(EventConstants.COMPONENT_DELETED_UEI, new String[] { "ServiceElementComponent", "lspB-PE2-PE1", "NA-SvcElemComp",    "9876,LSP-4321" });

		setupLogging("DEBUG");

		final MockHttpServletRequest request = createRequest(POST, "/NCS");
		request.setContentType(MediaType.APPLICATION_XML);
		request.setContent(m_serviceXMLFragment.getBytes());
		request.setQueryString("deleteOrphans=true");
		sendRequest(request, 200);
	}

	/*
	 * Deletes everything but the top-level "Service" component.
	 */
	@Test
	public void testDeleteOrphansRecursive() throws Exception {
		sendPost("/NCS", m_serviceXML);

		m_eventAnticipator.reset();

		setupLogging("DEBUG");

		// skip the 1st, since it will be "updated" instead of "deleted"
		for (int i = 1; i < m_components.length; i++) {
			anticipateEvent(EventConstants.COMPONENT_DELETED_UEI, m_components[i]);
		}
		anticipateEvent(EventConstants.COMPONENT_UPDATED_UEI, new String[] { "Service", "CokeP2P", "NA-Service", "123" });

		final MockHttpServletRequest request = createRequest(POST, "/NCS");
		request.setContentType(MediaType.APPLICATION_XML);
		request.setContent(m_serviceXMLTopFragment.getBytes());
		request.setQueryString("deleteOrphans=true");
		sendRequest(request, 200);
	}

	@Test
	public void testMultiParent() throws Exception {
		createMultiParent();

		final String xml = sendRequest(GET, "/NCS/top/topFs1:topFd1", 200);
		assertTrue(xml.contains("topFs1"));
		assertTrue(xml.contains("topFd1"));
		assertTrue(xml.contains("child1Fs1"));
		assertTrue(xml.contains("child1Fd1"));
		assertTrue(xml.contains("child1Fd2"));
		assertTrue(xml.contains("child2Fs1"));
		assertTrue(xml.contains("child2Fd1"));
	}

	@Test
	public void testDeleteMultiParentOrphans() throws Exception {
		setupLogging("DEBUG");

		createMultiParent();

		/*
		 * we should now have a tree of:
		 * 
		 * Top1
		 *   Child1-1
		 *     Child2-1+
		 *   Child1-2
		 *     Child2-1+
		 * 
		 * + should be the same object
		 */

		m_eventAnticipator.verifyAnticipated();
		m_eventAnticipator.reset();
		setupLogging("DEBUG");

		anticipateEvent(EventConstants.COMPONENT_UPDATED_UEI, new String[] { "top",    "Top1",     "topFs1",    "topFd1" });
		anticipateEvent(EventConstants.COMPONENT_UPDATED_UEI, new String[] { "child1", "Child1-2", "child1Fs1", "child1Fd2" });
		anticipateEvent(EventConstants.COMPONENT_DELETED_UEI, new String[] { "child1", "Child1-1", "child1Fs1", "child1Fd1" });
		sendRequest(DELETE, "/NCS/child1/child1Fs1:child1Fd1", parseParamData("deleteOrphans=true"), 200);

		String xml = sendRequest(GET, "/NCS/top/topFs1:topFd1", 200);
		assertFalse(xml.contains("child1Fd1"));
		assertTrue(xml.contains("child2Fd1"));
	}

	private void createMultiParent() throws Exception {
		// create a simple 3-level tree
		String text = "" +
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
				"<component xmlns=\"http://xmlns.opennms.org/xsd/model/ncs\" type=\"top\" foreignSource=\"topFs1\" foreignId=\"topFd1\">\n" +
				"  <name>Top1</name>\n" +
				"  <component type=\"child1\" foreignSource=\"child1Fs1\" foreignId=\"child1Fd1\">\n" +
				"    <name>Child1-1</name>\n" +
				"    <component type=\"child2\" foreignSource=\"child2Fs1\" foreignId=\"child2Fd1\">\n" +
				"      <name>Child2-1</name>\n" +
				"    </component>\n" +
				"  </component>\n" +
				"</component>\n";
		anticipateEvent(EventConstants.COMPONENT_ADDED_UEI, new String[] { "top",    "Top1",     "topFs1",    "topFd1" });
		anticipateEvent(EventConstants.COMPONENT_ADDED_UEI, new String[] { "child1", "Child1-1", "child1Fs1", "child1Fd1" });
		anticipateEvent(EventConstants.COMPONENT_ADDED_UEI, new String[] { "child2", "Child2-1", "child2Fs1", "child2Fd1" });
		sendPost("/NCS", text, 200);

		// create another "child1" type with the same "child2" type under it
		text = "" +
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"  +
				// note the foreignId is different
				"  <component xmlns=\"http://xmlns.opennms.org/xsd/model/ncs\" type=\"child1\" foreignSource=\"child1Fs1\" foreignId=\"child1Fd2\">\n" +
				"    <name>Child1-2</name>\n" +
				// but the child2 component is the same
				"    <component type=\"child2\" foreignSource=\"child2Fs1\" foreignId=\"child2Fd1\">\n" +
				"      <name>Child2-1</name>\n" +
				"    </component>\n" +
				"  </component>\n";
		anticipateEvent(EventConstants.COMPONENT_UPDATED_UEI, new String[] { "top",    "Top1",     "topFs1",    "topFd1" });
		anticipateEvent(EventConstants.COMPONENT_ADDED_UEI,   new String[] { "child1", "Child1-2", "child1Fs1", "child1Fd2" });
		anticipateEvent(EventConstants.COMPONENT_UPDATED_UEI, new String[] { "child2", "Child2-1", "child2Fs1", "child2Fd1" });
		sendPost("/NCS/top/topFs1:topFd1", text, 200);
	}

	private String formatParms(final List<Parm> parms) {
		final StringBuilder sb = new StringBuilder();
		sb.append("[");
		if (parms.size() > 0) {
			final Iterator<Parm> parmIterator = parms.iterator();
			while (parmIterator.hasNext()) {
				final Parm parm = parmIterator.next();
				sb.append("'").append(parm.getParmName()).append("'='");
				sb.append(parm.getValue().getContent()).append("'");
				if (parmIterator.hasNext()) sb.append(",");
			}
		}
		sb.append("]");
		return sb.toString();
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
