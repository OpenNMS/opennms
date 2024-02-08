/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.dao.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JaxbUtilsTest {
    private static final Logger LOG = LoggerFactory.getLogger(JaxbUtilsTest.class);
    
    private static final String m_xmlWithNamespace = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<event uuid=\"1234\" xmlns=\"http://xmlns.opennms.org/xsd/event\">"
              + "<dbid>37</dbid>"
              + "<dist-poller>localhost</dist-poller>"
              + "<creation-time>2011-03-18T09:34:13-04:00</creation-time>"
              + "<master-station>chief</master-station>"
              + "<mask>"
                + "<maskelement>"
                  + "<mename>generic</mename>"
                  + "<mevalue>6</mevalue>"
                + "</maskelement>"
              + "</mask>"
              + "<uei>uei.opennms.org/test</uei>"
              + "<source>JaxbUtilsTest</source>"
              + "<nodeid>1</nodeid>"
              + "<time>2011-03-18T09:34:13-04:00</time>"
              + "<host>funkytown</host>"
              + "<interface>192.168.0.1</interface>"
              + "<snmphost>192.168.0.1</snmphost>"
              + "<service>ICMP</service>"
              + "<snmp>"
                + "<id>.1.3.6.15</id>"
                + "<idtext>I am a banana!</idtext>"
                + "<version>v2c</version>"
                + "<specific>0</specific>"
                + "<generic>6</generic>"
                + "<community>public</community>"
                + "<time-stamp>1300455253196</time-stamp>"
              + "</snmp>"
              + "<parms>"
                + "<parm><parmName>foo</parmName><value encoding=\"text\" type=\"string\">bar</value></parm>"
              + "</parms>"
              + "<descr>This is a test thingy.</descr>"
              + "<logmsg dest=\"logndisplay\" notify=\"true\">this is a log message</logmsg>"
              + "<severity>Indeterminate</severity>"
              + "<pathoutage>monkeys</pathoutage>"
              + "<correlation path=\"pathOutage\" state=\"on\">"
                + "<cuei>uei.opennms.org/funky-stuff</cuei>"
                + "<cmin>1</cmin>"
                + "<cmax>17</cmax>"
                + "<ctime>yesterday</ctime>"
              + "</correlation>"
              + "<operinstruct>run away</operinstruct>"
              + "<autoaction state=\"off\">content</autoaction>"
              + "<operaction menutext=\"this is in the menu!\" state=\"on\">totally actiony</operaction>"
              + "<autoacknowledge state=\"off\">content</autoacknowledge>"
              + "<loggroup>foo</loggroup>"
              + "<loggroup>bar</loggroup>"
              + "<tticket state=\"on\">tticket stuff</tticket>"
              + "<forward mechanism=\"snmptcp\" state=\"on\">I like shoes.</forward>"
              + "<script language=\"zombo\">the unattainable is within reach, at zombo.com</script>"
              + "<ifIndex>53</ifIndex>"
              + "<ifAlias>giggetE</ifAlias>"
              + "<mouseovertext>click here to buy now!!!!1!1!</mouseovertext>"
              + "<alarm-data x733-probable-cause=\"27\" x733-alarm-type=\"TimeDomainViolation\" auto-clean=\"true\" clear-key=\"car\" alarm-type=\"19\" reduction-key=\"bus\"/>"
            + "</event>";
    private static final String m_logXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<log xmlns=\"http://xmlns.opennms.org/xsd/event\">"
              + "<events>"
                + "<event>"
                  + "<creation-time>2011-03-21T16:34:21-04:00</creation-time>"
                  + "<uei>uei.opennms.org/test</uei>"
                  + "<source>JaxbUtilsTest</source>"
                  + "<time>2011-03-21T16:34:21-04:00</time>"
                  + "<descr>test</descr>"
                + "</event>"
                + "<event>"
                  + "<creation-time>2011-03-21T16:34:21-04:00</creation-time>"
                  + "<uei>uei.opennms.org/test</uei>"
                  + "<source>JaxbUtilsTest</source>"
                  + "<time>2011-03-21T16:34:21-04:00</time>"
                  + "<descr>test 2</descr>"
                + "</event>"
              + "</events>"
            + "</log>";
    private static final String m_logXmlWithoutNamespace = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<log>"
              + "<events>"
                + "<event>"
                  + "<creation-time>2011-03-21T16:34:21-04:00</creation-time>"
                  + "<uei>uei.opennms.org/test</uei>"
                  + "<source>JaxbUtilsTest</source>"
                  + "<time>2011-03-21T16:34:21-04:00</time>"
                  + "<descr>test</descr>"
                + "</event>"
                + "<event>"
                  + "<creation-time>2011-03-21T16:34:21-04:00</creation-time>"
                  + "<uei>uei.opennms.org/test</uei>"
                  + "<source>JaxbUtilsTest</source>"
                  + "<time>2011-03-21T16:34:21-04:00</time>"
                  + "<descr>test 2</descr>"
                + "</event>"
              + "</events>"
            + "</log>";

	@Before
	public void setUp() {
		Locale.setDefault(Locale.US);
		MockLogAppender.setupLogging();
	}
	
	@After
	public void tearDown() {
        MockLogAppender.assertNoWarningsOrGreater();
	}

	@Test
	public void testMarshalEvent() throws Exception {
		final Event e = getEvent();
		final String xml = JaxbUtils.marshal(e);
		assertTrue(xml.contains("JaxbUtilsTest"));

		LOG.debug("event = {}", e);
		LOG.debug("xml = {}", xml);

		final StringWriter sw = new StringWriter();
		JaxbUtils.marshal(e, sw);
		assertEquals(sw.toString(), xml);

        final SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        final InputStream is = this.getClass().getResourceAsStream("/xsds/event.xsd");
        // if this is null, it's because Eclipse can be confused by "classifier" test dependencies like opennms-model-*-xsds
        // it only works if opennms-model is *not* pulled into eclipse (go figure)
        Assume.assumeNotNull(is);
        
        LOG.debug("Hooray!  We have an XSD!");
        final Schema schema = factory.newSchema(new StreamSource(is));
        final Validator v = schema.newValidator();
        v.validate(new StreamSource(new StringReader(xml)));
	}

	@Test
	public void testUnmarshalEvent() throws Exception {
		final Event event = JaxbUtils.unmarshal(Event.class, m_xmlWithNamespace);
		LOG.debug("event = {}", event);
		assertEquals("1234", event.getUuid());
		assertEquals("192.168.0.1", event.getInterface());
	}

	@Test
	public void testMarshalLog() throws Exception {
		final Event e1 = getEvent();
		final Event e2 = getEvent();
		e2.setDescr("test 2");
		
		final Events events = new Events();
		events.addEvent(e1);
		events.addEvent(e2);
		
		final Log log = new Log();
		log.setEvents(events);
		
		final String xml = JaxbUtils.marshal(log);

		LOG.debug("xml = {}", xml);
		assertNotNull(xml);
		assertTrue(xml.contains("JaxbUtilsTest"));
	}

	@Test
	public void testUnmarshalLog() throws Exception {
		final Log log = JaxbUtils.unmarshal(Log.class, m_logXml);
		assertNotNull(log.getEvents());
		assertEquals(2, log.getEvents().getEventCount());
		assertEquals("JaxbUtilsTest", log.getEvents().getEvent(0).getSource());
		assertEquals(1300739661000L, log.getEvents().getEvent(0).getTime().getTime());
	}

	@Test
	public void testUnmarshalLogNoNamespace() throws Exception {
		final Log log = JaxbUtils.unmarshal(Log.class, m_logXmlWithoutNamespace);
		assertNotNull(log.getEvents());
		assertEquals(2, log.getEvents().getEventCount());
		assertEquals("JaxbUtilsTest", log.getEvents().getEvent(0).getSource());
		assertEquals(1300739661000L, log.getEvents().getEvent(0).getTime().getTime());
	}

	private Event getEvent() {
		final EventBuilder eb = new EventBuilder("uei.opennms.org/test", "JaxbUtilsTest");
		final Event e = eb
			.setDescription("test")
			.addParam("foo", "bar")
			.getEvent();
		return e;
	}
	
	@Test
	public void testSendEventXml() throws Exception {
		final String text = "<log>\n" + 
				" <events>\n" + 
				"  <event >\n" + 
				"   <uei>uei.opennms.org/internal/capsd/addNode</uei>\n" + 
				"   <source>perl_send_event</source>\n" + 
				"   <time>2011-04-12T18:05:00-00:00</time>\n" + 
				"   <host></host>\n" + 
				"   <interface>10.0.0.1</interface>\n" + 
				"   <parms>\n" + 
				"    <parm>\n" + 
				"     <parmName><![CDATA[txno]]></parmName>\n" + 
				"     <value type=\"string\" encoding=\"text\"><![CDATA[1]]></value>\n" + 
				"    </parm>\n" + 
				"    <parm>\n" + 
				"     <parmName><![CDATA[nodelabel]]></parmName>\n" + 
				"     <value type=\"string\" encoding=\"text\"><![CDATA[test10]]></value>\n" + 
				"    </parm>\n" + 
				"   </parms>\n" + 
				"  </event>\n" + 
				" </events>\n" + 
				"</log>\n";
		
		final Log log = JaxbUtils.unmarshal(Log.class, text);
		assertNotNull(log);
		assertNotNull(log.getEvents());
		assertEquals(1, log.getEvents().getEvent().length);
		// Make sure that the time was parsed properly to a specific epoch time
		assertEquals(1302631500000L, log.getEvents().getEvent(0).getTime().getTime());
	}
}
