/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.xml.CastorUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;

public class JaxbUtilsTest {
    private static final Logger LOG = LoggerFactory.getLogger(JaxbUtilsTest.class);
    
    private static final String m_xmlWithNamespace = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><event uuid=\"1234\" xmlns=\"http://xmlns.opennms.org/xsd/event\"><dbid>37</dbid><dist-poller>localhost</dist-poller><creation-time>Friday, March 18, 2011 1:34:13 PM GMT</creation-time><master-station>chief</master-station><mask><maskelement><mename>generic</mename><mevalue>6</mevalue></maskelement></mask><uei>uei.opennms.org/test</uei><source>JaxbCastorEquivalenceTest</source><nodeid>1</nodeid><time>Friday, March 18, 2011 1:34:13 PM GMT</time><host>funkytown</host><interface>192.168.0.1</interface><snmphost>192.168.0.1</snmphost><service>ICMP</service><snmp><id>.1.3.6.15</id><idtext>I am a banana!</idtext><version>v2c</version><specific>0</specific><generic>6</generic><community>public</community><time-stamp>1300455253196</time-stamp></snmp><parms><parm><parmName>foo</parmName><value encoding=\"text\" type=\"string\">bar</value></parm></parms><descr>This is a test thingy.</descr><logmsg dest=\"logndisplay\" notify=\"true\">this is a log message</logmsg><severity>Indeterminate</severity><pathoutage>monkeys</pathoutage><correlation path=\"pathOutage\" state=\"on\"><cuei>uei.opennms.org/funky-stuff</cuei><cmin>1</cmin><cmax>17</cmax><ctime>yesterday</ctime></correlation><operinstruct>run away</operinstruct><autoaction state=\"off\">content</autoaction><operaction menutext=\"this is in the menu!\" state=\"on\">totally actiony</operaction><autoacknowledge state=\"off\">content</autoacknowledge><loggroup>foo</loggroup><loggroup>bar</loggroup><tticket state=\"on\">tticket stuff</tticket><forward mechanism=\"snmptcp\" state=\"on\">I like shoes.</forward><script language=\"zombo\">the unattainable is within reach, at zombo.com</script><ifIndex>53</ifIndex><ifAlias>giggetE</ifAlias><mouseovertext>click here to buy now!!!!1!1!</mouseovertext><alarm-data x733-probable-cause=\"27\" x733-alarm-type=\"TimeDomainViolation\" auto-clean=\"true\" clear-key=\"car\" alarm-type=\"19\" reduction-key=\"bus\"/></event>";
    private static final String m_logXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><log xmlns=\"http://xmlns.opennms.org/xsd/event\"><events><event><creation-time>Monday, March 21, 2011 8:34:21 PM GMT</creation-time><uei>uei.opennms.org/test</uei><source>JaxbUtilsTest</source><time>Monday, March 21, 2011 8:34:21 PM GMT</time><descr>test</descr></event><event><creation-time>Monday, March 21, 2011 8:34:21 PM GMT</creation-time><uei>uei.opennms.org/test</uei><source>JaxbUtilsTest</source><time>Monday, March 21, 2011 8:34:21 PM GMT</time><descr>test 2</descr></event></events></log>";
    private static final String m_logXmlWithoutNamespace = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><log><events><event><creation-time>Monday, March 21, 2011 8:34:21 PM GMT</creation-time><uei>uei.opennms.org/test</uei><source>JaxbUtilsTest</source><time>Monday, March 21, 2011 8:34:21 PM GMT</time><descr>test</descr></event><event><creation-time>Monday, March 21, 2011 8:34:21 PM GMT</creation-time><uei>uei.opennms.org/test</uei><source>JaxbUtilsTest</source><time>Monday, March 21, 2011 8:34:21 PM GMT</time><descr>test 2</descr></event></events></log>";

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
		
		final InputStream is = new ByteArrayInputStream(m_logXml.getBytes());
		final Log log2 = CastorUtils.unmarshal(Log.class, is);
		is.close();
		
		assertNotNull(log2.getEvents());
		assertEquals(2, log2.getEvents().getEventCount());
		assertEquals("JaxbUtilsTest", log2.getEvents().getEvent(0).getSource());
		assertNotNull(log2.getEvents().getEvent(0).getTime());
		LOG.debug("castor log = {}", log2);
	}
	
    /**
     * This test can be used to compare the performance of JAXB vs. Castor in XML unmarshalling speed.
     * After running this test on my system when preparing for the OpenNMS 1.10 release, JAXB was
     * roughly 30% faster than Castor.
     */
    @Test
    @Ignore
    public void testUnmarshalLogCastorVersusJaxb() throws Exception {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            JaxbUtils.unmarshal(Log.class, m_logXml);
        }
        long jaxbTime = System.currentTimeMillis() - startTime;

        final byte[] logBytes = m_logXml.getBytes();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            CastorUtils.unmarshal(Log.class, new ByteArrayInputStream(logBytes));
        }
        long castorTime = System.currentTimeMillis() - startTime;
        
        System.out.printf("JAXB unmarshal: %dms, Castor unmarshal: %dms\n", jaxbTime, castorTime);
    }
    
    /**
     * This test can be used to compare the performance of JAXB vs. Castor in XML marshalling speed.
     * After running this test on my system when preparing for the OpenNMS 1.10 release, JAXB was
     * roughly 8 times faster than Castor.
     */
    @Test
    @Ignore
    public void testMarshalLogCastorVersusJaxb() throws Exception {
        Log log = JaxbUtils.unmarshal(Log.class, m_logXml);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            JaxbUtils.marshal(log);
            // If you want to see the marshalled XML output...
            //System.out.println(JaxbUtils.marshal(log));
        }
        long jaxbTime = System.currentTimeMillis() - startTime;

        log = CastorUtils.unmarshal(Log.class, new ByteArrayInputStream(m_logXml.getBytes()));
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            CastorUtils.marshalWithTranslatedExceptions(log, new StringWriter());
            // If you want to see the marshalled XML output...
            //CastorUtils.marshalWithTranslatedExceptions(log, new OutputStreamWriter(System.out));
        }
        long castorTime = System.currentTimeMillis() - startTime;
        
        System.out.printf("JAXB marshal: %dms, Castor marshal: %dms\n", jaxbTime, castorTime);
    }
    
	@Test
	public void testUnmarshalLogNoNamespace() throws Exception {
		final Log log = JaxbUtils.unmarshal(Log.class, m_logXmlWithoutNamespace);
		assertNotNull(log.getEvents());
		assertEquals(2, log.getEvents().getEventCount());
		assertEquals("JaxbUtilsTest", log.getEvents().getEvent(0).getSource());
		
		final InputStream is = new ByteArrayInputStream(m_logXmlWithoutNamespace.getBytes());
		final Log log2 = CastorUtils.unmarshal(Log.class, is);
		is.close();
		
		assertNotNull(log2.getEvents());
		assertEquals(2, log2.getEvents().getEventCount());
		assertEquals("JaxbUtilsTest", log2.getEvents().getEvent(0).getSource());
		assertNotNull(log2.getEvents().getEvent(0).getTime());
		LOG.debug("castor log = {}", log2);
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
				"   <time>Tuesday, 12 April 2011 18:05:00 o'clock GMT</time>\n" + 
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
	}
	
	@Test
	@Ignore
	public void testValidationMemoryLeak() throws Exception {
        final String text = "<log>\n" + 
            " <events>\n" + 
            "  <event >\n" + 
            "   <uei>uei.opennms.org/internal/capsd/addNode</uei>\n" + 
            "   <source>perl_send_event</source>\n" + 
            "   <time>Tuesday, 12 April 2011 18:05:00 o'clock GMT</time>\n" + 
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
        
        final int eventCount = 1000000;
        final int logEvery = (eventCount / 1000);
        
        MockLogAppender.setupLogging(true, "INFO");

        LOG.info("starting");
        Thread.sleep(30000);
        for (int i = 0; i < eventCount; i++) {
            if (i % logEvery == 0) {
                LOG.info("- event #{}", i);
            }
            final Log log = JaxbUtils.unmarshal(Log.class, text);
            assertNotNull(log);
            assertNotNull(log.getEvents());
            final String results = JaxbUtils.marshal(log);
            assertNotNull(results);
            assertTrue(results.contains("uei.opennms.org/internal/capsd/addNode"));
        }
        LOG.info("finished");
        Thread.sleep(30000);
	}
}
