/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.model.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.exolab.castor.xml.Marshaller;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.xml.SimpleNamespaceFilter;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Autoacknowledge;
import org.opennms.netmgt.xml.event.Autoaction;
import org.opennms.netmgt.xml.event.Correlation;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Forward;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.netmgt.xml.event.Mask;
import org.opennms.netmgt.xml.event.Maskelement;
import org.opennms.netmgt.xml.event.Operaction;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Script;
import org.opennms.netmgt.xml.event.Snmp;
import org.opennms.netmgt.xml.event.Tticket;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


public class JaxbCastorEquivalenceTest {

	private static final String xmlWithNamespace = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><event uuid=\"1234\" xmlns=\"http://xmlns.opennms.org/xsd/event\"><dbid>37</dbid><dist-poller>localhost</dist-poller><creation-time>Friday, March 18, 2011 1:34:13 PM GMT</creation-time><master-station>chief</master-station><mask><maskelement><mename>generic</mename><mevalue>6</mevalue></maskelement></mask><uei>uei.opennms.org/test</uei><source>JaxbCastorEquivalenceTest</source><nodeid>1</nodeid><time>Friday, March 18, 2011 1:34:13 PM GMT</time><host>funkytown</host><interface>192.168.0.1</interface><snmphost>192.168.0.1</snmphost><service>ICMP</service><snmp><id>.1.3.6.15</id><idtext>I am a banana!</idtext><version>v2c</version><specific>0</specific><generic>6</generic><community>public</community><time-stamp>1300455253196</time-stamp></snmp><parms><parm><parmName>foo</parmName><value encoding=\"text\" type=\"string\">bar</value></parm></parms><descr>This is a test thingy.</descr><logmsg dest=\"logndisplay\" notify=\"true\">this is a log message</logmsg><severity>Indeterminate</severity><pathoutage>monkeys</pathoutage><correlation path=\"pathOutage\" state=\"on\"><cuei>uei.opennms.org/funky-stuff</cuei><cmin>1</cmin><cmax>17</cmax><ctime>yesterday</ctime></correlation><operinstruct>run away</operinstruct><autoaction state=\"off\">content</autoaction><operaction menutext=\"this is in the menu!\" state=\"on\">totally actiony</operaction><autoacknowledge state=\"off\">content</autoacknowledge><loggroup>foo</loggroup><loggroup>bar</loggroup><tticket state=\"on\">tticket stuff</tticket><forward mechanism=\"snmptcp\" state=\"on\">I like shoes.</forward><script language=\"zombo\">the unattainable is within reach, at zombo.com</script><ifIndex>53</ifIndex><ifAlias>giggetE</ifAlias><mouseovertext>click here to buy now!!!!1!1!</mouseovertext><alarm-data x733-probable-cause=\"27\" x733-alarm-type=\"TimeDomainViolation\" auto-clean=\"true\" clear-key=\"car\" alarm-type=\"19\" reduction-key=\"bus\"/></event>";

	@Before
	public void setUp() {
		MockLogAppender.setupLogging(true);
	}

	@Test
	@Ignore("castor-specific things have been removed from the classes now")
	public void marshalEvent() throws Exception {
		final Event event = getFullEvent();

		final StringWriter jaxbWriter = new StringWriter();
        final JAXBContext c = JAXBContext.newInstance("org.opennms.netmgt.xml.event");
        final javax.xml.bind.Marshaller jaxbMarshaller = c.createMarshaller();

        final SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        final Schema schema = factory.newSchema(new StreamSource("src/main/castor/event.xsd"));
        jaxbMarshaller.setSchema(schema);
		jaxbMarshaller.marshal(event, jaxbWriter);
        final String jaxbXml = jaxbWriter.toString();
		System.err.println("JAXB:   " + jaxbXml);

		final StringWriter castorWriter = new StringWriter();
		final Marshaller m = new Marshaller(castorWriter);
        m.setSuppressNamespaces(true);
        m.marshal(event);
        final String castorXml = castorWriter.toString();
        final String formattedCastorXml = castorXml.replaceFirst("<event ", "<event xmlns=\"http://xmlns.opennms.org/xsd/event\" ").replaceFirst("\\?>\n", " standalone=\"yes\"?>");
		System.err.println("Castor: " + formattedCastorXml);

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalize(true);
        
        assertXmlEquals(formattedCastorXml, jaxbXml);

        org.exolab.castor.xml.Unmarshaller castorUnmarshaller = new org.exolab.castor.xml.Unmarshaller(Event.class);
        castorUnmarshaller.setIgnoreExtraAttributes(false);
        castorUnmarshaller.setIgnoreExtraElements(false);
        castorUnmarshaller.setWhitespacePreserve(true);
        final Reader jaxbStringReader = new StringReader(jaxbXml);
        final InputSource jaxbInputSource = new InputSource(jaxbStringReader);
        final Event jaxbEvent = (Event)castorUnmarshaller.unmarshal(jaxbInputSource);
        System.err.println("event = " + jaxbEvent);

        final Unmarshaller jaxbUnmarshaller = c.createUnmarshaller();
        jaxbUnmarshaller.setSchema(schema);

        final XMLReader jaxbReader = XMLReaderFactory.createXMLReader();
        final StringReader castorXmlReader = new StringReader(castorXml);
        final InputSource xmlSource = new InputSource(castorXmlReader);

        final SimpleNamespaceFilter filter = new SimpleNamespaceFilter("http://xmlns.opennms.org/xsd/event", true);
        filter.setParent(jaxbReader);

        final SAXSource source = new SAXSource(filter, xmlSource);
        Event newCastorEvent = (Event)jaxbUnmarshaller.unmarshal(source);
        assertNotNull(newCastorEvent);
	}

    @Test
    public void testUnmarshalEventSimple() throws Exception {
        final JAXBContext c = JAXBContext.newInstance("org.opennms.netmgt.xml.event");
        c.createUnmarshaller().unmarshal(new StringReader(xmlWithNamespace));
    }

    @Test
    public void testUnmarshalEventSimpleWithValidation() throws Exception {
        final JAXBContext c = JAXBContext.newInstance("org.opennms.netmgt.xml.event");
        final Unmarshaller unmarshaller = c.createUnmarshaller();
        final SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        final Schema schema = factory.newSchema(new StreamSource("src/main/castor/event.xsd"));
        unmarshaller.setSchema(schema);
        unmarshaller.unmarshal(new StringReader(xmlWithNamespace));
    }

    @Test
    public void testUnmarshalEventSimpleWithValidationAndGeneratedJaxb() throws Exception {
        final JAXBContext c = JAXBContext.newInstance("org.opennms.xmlns.xsd.event");
        final Unmarshaller unmarshaller = c.createUnmarshaller();
        final SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        final Schema schema = factory.newSchema(new StreamSource("src/main/castor/event.xsd"));
        unmarshaller.setSchema(schema);
        unmarshaller.unmarshal(new StringReader(xmlWithNamespace));
    }

	private Event getFullEvent() {
		final EventBuilder builder = new EventBuilder("uei.opennms.org/test", "JaxbCastorEquivalenceTest");
		final Event event = builder
			.setUuid("1234")
			.setDistPoller("localhost")
			.setMasterStation("chief")
			.setNodeid(1)
			.setHost("funkytown")
			.setInterface(addr("192.168.0.1"))
			.setSnmpHost("192.168.0.1")
			.setService("ICMP")
			.setDescription("This is a test thingy.")
			.setSeverity("normal")
			.getEvent();
		event.setDbid(37);
		event.setMask(getFullMask());
		event.setSnmp(getFullSnmp());
		event.setParmCollection(getParmCollection());
		event.setLogmsg(getFullLogmsg());
		event.setPathoutage("monkeys");
		event.setCorrelation(getFullCorrelation());
		event.setOperinstruct("run away");
		event.setOperaction(new Operaction[] { getFullOperaction() });
		event.setAutoaction(new Autoaction[] { getFullAutoaction() });
		event.setAutoacknowledge(getFullAutoacknowledge());
		event.setLoggroup(new String[] { "foo", "bar" });
		event.setTticket(getFullTticket());
		event.setForward(new Forward[] { getFullForward() });
		event.setScript(new Script[] { getFullScript() });
		event.setIfIndex(53);
		event.setIfAlias("giggetE");
		event.setMouseovertext("click here to buy now!!!!1!1!");
		event.setAlarmData(getFullAlarmData());
		return event;
	}

	private AlarmData getFullAlarmData() {
		final AlarmData alarmData = new AlarmData();
		alarmData.setAlarmType(19);
		alarmData.setAutoClean(true);
		alarmData.setClearKey("car");
		alarmData.setReductionKey("bus");
		alarmData.setX733AlarmType("TimeDomainViolation");
		alarmData.setX733ProbableCause(27);
		return alarmData;
	}

	private Script getFullScript() {
		final Script script = new Script();
		script.setLanguage("zombo");
		script.setContent("the unattainable is within reach, at zombo.com");
		return script;
	}

	private Forward getFullForward() {
		final Forward forward = new Forward();
		forward.setContent("I like shoes.");
		forward.setMechanism("snmptcp");
		forward.setState("on");
		return forward;
	}

	private Tticket getFullTticket() {
		final Tticket tticket = new Tticket();
		tticket.setContent("tticket stuff");
		tticket.setState("on");
		return tticket;
	}

	private Autoacknowledge getFullAutoacknowledge() {
		final Autoacknowledge autoacknowledge = new Autoacknowledge();
		autoacknowledge.setContent("content");
		autoacknowledge.setState("off");
		return autoacknowledge;
	}

	private Autoaction getFullAutoaction() {
		final Autoaction autoaction = new Autoaction();
		autoaction.setContent("content");
		autoaction.setState("off");
		return autoaction;
	}

	private Operaction getFullOperaction() {
		final Operaction operaction = new Operaction();
		operaction.setContent("totally actiony");
		operaction.setMenutext("this is in the menu!");
		operaction.setState("on");
		return operaction;
	}

	private Correlation getFullCorrelation() {
		final Correlation correlation = new Correlation();
		correlation.setCmax("17");
		correlation.setCmin("1");
		correlation.setCtime("yesterday");
		correlation.setCuei(new String[] { "uei.opennms.org/funky-stuff" });
		correlation.setPath("pathOutage");
		correlation.setState("on");
		return correlation;
	}

	private Logmsg getFullLogmsg() {
		final Logmsg logmsg = new Logmsg();
		logmsg.setContent("this is a log message");
		return logmsg;
	}

	private List<Parm> getParmCollection() {
	    final List<Parm> parms = new ArrayList<Parm>();
	    parms.add(new Parm("foo", "bar"));
	    return parms;
	}

	private Snmp getFullSnmp() {
		final Snmp snmp = new Snmp();
		snmp.setCommunity("public");
		snmp.setGeneric(6);
		snmp.setId(".1.3.6.15");
		snmp.setIdtext("I am a banana!");
		snmp.setSpecific(0);
		snmp.setTimeStamp(new Date().getTime());
		snmp.setVersion("v2c");
		return snmp;
	}

	private Mask getFullMask() {
		final Mask mask = new Mask();
		mask.setMaskelement(new Maskelement[] { getFullMaskelement() });
		return mask;
	}

	private Maskelement getFullMaskelement() {
		final Maskelement maskelement = new Maskelement();
		maskelement.setMename("generic");
		maskelement.setMevalue(new String[] { "6" });
		return maskelement;
	}
	
	private void assertXmlEquals(final String string, final String string2) throws Exception {
        final DetailedDiff diff = getDiff(string, string2);
        System.err.println("diff = " + diff);
        assertEquals("number of XMLUnit differences between the example xml and the mock object xml is 0", 0, diff.getAllDifferences().size());
	}

	private DetailedDiff getDiff(final String xmlA, final String xmlB) throws SAXException, IOException {
        DetailedDiff myDiff = new DetailedDiff(XMLUnit.compareXML(xmlA, xmlB));
        @SuppressWarnings("unchecked")
		List<Difference> allDifferences = myDiff.getAllDifferences();
        if (allDifferences.size() > 0) {
            for (Difference d : allDifferences) {
                System.err.println(d);
            }
        }
        return myDiff;
    }
}
