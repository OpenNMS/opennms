package org.opennms.netmgt.model.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.exolab.castor.xml.Marshaller;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.xml.SimpleNamespaceFilter;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Autoacknowledge;
import org.opennms.netmgt.xml.event.Autoaction;
import org.opennms.netmgt.xml.event.Correlation;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.EventReceipt;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Forward;
import org.opennms.netmgt.xml.event.Header;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.netmgt.xml.event.Mask;
import org.opennms.netmgt.xml.event.Maskelement;
import org.opennms.netmgt.xml.event.Operaction;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Script;
import org.opennms.netmgt.xml.event.Snmp;
import org.opennms.netmgt.xml.event.Tticket;
import org.opennms.netmgt.xml.event.Value;
import org.opennms.test.mock.MockLogAppender;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
public class JaxbCastorEquivalenceTest {

	@Before
	public void setUp() {
		MockLogAppender.setupLogging(true);
		System.setProperty("jaxb.debug", "true");
	}

	@Test
	public void marshalEvent() throws Exception {
		final Event event = getFullEvent();
		
        StringWriter jaxbWriter = new StringWriter();
        final JAXBContext c = JAXBContext.newInstance("org.opennms.netmgt.xml.event");
//        final JAXBContext c = JAXBContext.newInstance(AlarmData.class, Autoacknowledge.class, Autoaction.class, Correlation.class, Event.class, EventReceipt.class, Events.class, Forward.class, Header.class, Log.class, Logmsg.class, Mask.class, Maskelement.class, Operaction.class, Parm.class, Parms.class, Script.class, Snmp.class, Tticket.class, Value.class);
        final javax.xml.bind.Marshaller jaxbMarshaller = c.createMarshaller();

        /*
        String schemaLang = "http://www.w3.org/2001/XMLSchema";
        SchemaFactory factory = SchemaFactory.newInstance(schemaLang);
        Schema schema = factory.newSchema(new StreamSource("src/main/castor/event.xsd"));
        jaxbMarshaller.setSchema(schema);
        */
		jaxbMarshaller.marshal(event, jaxbWriter);
        final String jaxbXml = jaxbWriter.toString();
		System.err.println("JAXB:   " + jaxbXml);

		StringWriter castorWriter = new StringWriter();
        Marshaller m = new Marshaller(castorWriter);
        m.setSuppressNamespaces(true);
        m.marshal(event);
        String castorXml = castorWriter.toString();
        String formattedCastorXml = castorXml.replaceFirst("<event ", "<event xmlns=\"http://xmlns.opennms.org/xsd/event\" ").replaceFirst("\\?>\n", " standalone=\"yes\"?>");
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
        jaxbUnmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
        XMLReader jaxbReader = XMLReaderFactory.createXMLReader();
        StringReader castorXmlReader = new StringReader(castorXml);
        final InputSource xmlSource = new InputSource(castorXmlReader);

        final SimpleNamespaceFilter filter = new SimpleNamespaceFilter("http://xmlns.opennms.org/xsd/event", true);
        filter.setParent(jaxbReader);

        final SAXSource source = new SAXSource(filter, xmlSource);
        final Event castorEvent = (Event)jaxbUnmarshaller.unmarshal(source);
        assertNotNull(castorEvent);
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
		event.setParms(getFullParms());
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

	private Parms getFullParms() {
		final Parms parms = new Parms();
		final Parm parm = new Parm("foo", "bar");
		parms.addParm(parm);
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
        assertEquals("number of XMLUnit differences between the example XML and the mock object XML is 0", 0, diff.getAllDifferences().size());
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
