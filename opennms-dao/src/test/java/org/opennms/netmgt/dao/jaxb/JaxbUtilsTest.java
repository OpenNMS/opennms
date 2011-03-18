package org.opennms.netmgt.dao.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.mock.MockLogAppender;

public class JaxbUtilsTest {
    private static final String m_xmlWithNamespace = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><event uuid=\"1234\" xmlns=\"http://xmlns.opennms.org/xsd/event\"><dbid>37</dbid><dist-poller>localhost</dist-poller><creation-time>Friday, March 18, 2011 1:34:13 PM GMT</creation-time><master-station>chief</master-station><mask><maskelement><mename>generic</mename><mevalue>6</mevalue></maskelement></mask><uei>uei.opennms.org/test</uei><source>JaxbCastorEquivalenceTest</source><nodeid>1</nodeid><time>Friday, March 18, 2011 1:34:13 PM GMT</time><host>funkytown</host><interface>192.168.0.1</interface><snmphost>192.168.0.1</snmphost><service>ICMP</service><snmp><id>.1.3.6.15</id><idtext>I am a banana!</idtext><version>v2c</version><specific>0</specific><generic>6</generic><community>public</community><time-stamp>1300455253196</time-stamp></snmp><parms><parm><parmName>foo</parmName><value encoding=\"text\" type=\"string\">bar</value></parm></parms><descr>This is a test thingy.</descr><logmsg dest=\"logndisplay\" notify=\"true\">this is a log message</logmsg><severity>Indeterminate</severity><pathoutage>monkeys</pathoutage><correlation path=\"pathOutage\" state=\"on\"><cuei>uei.opennms.org/funky-stuff</cuei><cmin>1</cmin><cmax>17</cmax><ctime>yesterday</ctime></correlation><operinstruct>run away</operinstruct><autoaction state=\"off\">content</autoaction><operaction menutext=\"this is in the menu!\" state=\"on\">totally actiony</operaction><autoacknowledge state=\"off\">content</autoacknowledge><loggroup>foo</loggroup><loggroup>bar</loggroup><tticket state=\"on\">tticket stuff</tticket><forward mechanism=\"snmptcp\" state=\"on\">I like shoes.</forward><script language=\"zombo\">the unattainable is within reach, at zombo.com</script><ifIndex>53</ifIndex><ifAlias>giggetE</ifAlias><mouseovertext>click here to buy now!!!!1!1!</mouseovertext><alarm-data x733-probable-cause=\"27\" x733-alarm-type=\"TimeDomainViolation\" auto-clean=\"true\" clear-key=\"car\" alarm-type=\"19\" reduction-key=\"bus\"/></event>";

	@Before
	public void setUp() {
		MockLogAppender.setupLogging();
	}
	
	@After
	public void tearDown() {
        MockLogAppender.assertNoWarningsOrGreater();
	}

	@Test
	public void testMarshalEvent() throws Exception {
		final EventBuilder eb = new EventBuilder("uei.opennms.org/test", "JaxbUtilsTest");
		final Event e = eb
			.setDescription("test")
			.getEvent();
		final String xml = JaxbUtils.marshal(e);
		assertTrue(xml.contains("JaxbUtilsTest"));

		System.err.println("event = " + e);
		System.err.println("xml = " + xml);

		final StringWriter sw = new StringWriter();
		JaxbUtils.marshal(e, sw);
		assertEquals(sw.toString(), xml);

        final SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        final InputStream is = this.getClass().getResourceAsStream("/xsds/event.xsd");
        Assume.assumeNotNull(is); // if this is null, it's because Eclipse can be confused by "classifier" test dependencies like opennms-model-*-xsds
        
        final Schema schema = factory.newSchema(new StreamSource(is));
        final Validator v = schema.newValidator();
        v.validate(new StreamSource(new StringReader(xml)));
	}

	@Test
	public void testUnmarshalEvent() throws Exception {
		final Event event = JaxbUtils.unmarshal(Event.class, m_xmlWithNamespace);
		assertEquals("1234", event.getUuid());
	}
}
