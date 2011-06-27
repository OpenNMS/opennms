package org.opennms.netmgt.xml.eventconf;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTest;

public class EventTest extends XmlTest<Event> {

	public EventTest(final Event sampleObject, final String sampleXml,
			final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
//		String uei="uei.opennms.org/ietf/mplsTeStdMib/traps/mplsTunnelUp";
//		String eventlabel = "MPLS-TE-STD-MIB defined trap event: mplsTunnelUp";
//		String descr = "&lt;p&gt;This notification is generated when a\n" + 
//		"mplsTunnelOperStatus object for one of the\n" + 
//		"configured tunnels is about to leave the down state\n" + 
//		"and transition into some other state (but not into\n" + 
//		"the notPresent state).  This other state is\n" + 
//		"indicated by the included value of\n" + 
//		"mplsTunnelOperStatus.&lt;/p&gt;&lt;table&gt;\n" + 
//		"        &lt;tr&gt;&lt;td&gt;&lt;b&gt;\n" + 
//		"\n" + 
//		"        mplsTunnelAdminStatus&lt;/b&gt;&lt;/td&gt;&lt;td&gt;\n" + 
//		"        %parm[#1]%;&lt;/td&gt;&lt;td&gt;&lt;p&gt;\n" + 
//		"                up(1)\n" + 
//		"                down(2)\n" + 
//		"                testing(3)\n" + 
//		"        &lt;/p&gt;&lt;/td&gt;&lt;/tr&gt;\n" + 
//		"        &lt;tr&gt;&lt;td&gt;&lt;b&gt;\n" + 
//		"\n" + 
//		"        mplsTunnelOperStatus&lt;/b&gt;&lt;/td&gt;&lt;td&gt;\n" + 
//		"        %parm[#2]%;&lt;/td&gt;&lt;td&gt;&lt;p&gt;\n" + 
//		"                up(1)\n" + 
//		"                down(2)\n" + 
//		"                testing(3)\n" + 
//		"                unknown(4)\n" + 
//		"                dormant(5)\n" + 
//		"                notPresent(6)\n" + 
//		"                lowerLayerDown(7)\n" + 
//		"        &lt;/p&gt;&lt;/td&gt;&lt;/tr&gt;&lt;/table&gt;";
//		String logmsgContent = "&lt;p&gt;\n" + 
//		"        mplsTunnelUp trap received\n" + 
//		"        mplsTunnelAdminStatus=%parm[#1]%\n" + 
//		"        mplsTunnelOperStatus=%parm[#2]%&lt;/p&gt;";
//		String severity = "normal";
		Event event0 = new Event();
		event0.setUei("uei");
		event0.setEventLabel("event-label");
		event0.setDescr("descr");
		Logmsg logmsg0 = new Logmsg();
		logmsg0.setContent("log message");
		event0.setLogmsg(logmsg0);
		event0.setSeverity("normal");

		Event event1 = new Event();
		event1.setUei("uei");
		event1.setEventLabel("event-label");
		event1.setDescr("descr");
		event1.setLogmsg(logmsg0);
		event1.setSeverity("normal");
		Mask mask0 = new Mask();
		Maskelement maskelement0 = new Maskelement();
		maskelement0.setMename("id");
		maskelement0.addMevalue(".1.3.6.1.2.1.10.166.3");
		Maskelement maskelement1 = new Maskelement();
		maskelement1.setMename("generic");
		maskelement1.addMevalue("6");
		Maskelement maskelement2 = new Maskelement();
		maskelement2.setMename("specific");
		maskelement2.addMevalue("1");
		mask0.addMaskelement(maskelement0);
		mask0.addMaskelement(maskelement1);
		mask0.addMaskelement(maskelement2);
		Varbind varbind = new Varbind();
		varbind.setVbnumber(5);
		varbind.addVbvalue("0");
		mask0.addVarbind(varbind);
		event1.setMask(mask0);
		Snmp snmp0 = new Snmp();
		snmp0.setId(".1.3.6.1.4.1.9");
		snmp0.setVersion("v2c");
		snmp0.setGeneric(6);
		snmp0.setSpecific(3);
		snmp0.setIdtext("Test");
		snmp0.setCommunity("public");
		event1.setSnmp(snmp0);

		return Arrays.asList(new Object[][] {
				{event0,
				"<event>" +
				"<uei>uei</uei>" +
				"<event-label>event-label</event-label>" +
				"<descr>descr</descr>" +
				"<logmsg>log message</logmsg>" +
				"<severity>normal</severity>" +
				"</event>",
				"target/classes/xsds/eventconf.xsd" }, 
				{event1,
					"<event>" +
					"  <mask>\n" + 
					"    <maskelement>\n" + 
					"      <mename>id</mename>\n" + 
					"      <mevalue>.1.3.6.1.2.1.10.166.3</mevalue>\n" + 
					"    </maskelement>\n" + 
					"    <maskelement>\n" + 
					"      <mename>generic</mename>\n" + 
					"      <mevalue>6</mevalue>\n" + 
					"    </maskelement>\n" + 
					"    <maskelement>\n" + 
					"      <mename>specific</mename>\n" + 
					"      <mevalue>1</mevalue>\n" + 
					"    </maskelement>\n" + 
					"    <varbind>" +
					"      <vbnumber>5</vbnumber>" +
					"      <vbvalue>0</vbvalue>" +
					"    </varbind>" +
					"  </mask>\n" +
					"  <uei>uei</uei>" +
					"  <event-label>event-label</event-label>" +
					"  <snmp>" +
					"    <id>.1.3.6.1.4.1.9</id>" +
					"    <idtext>Test</idtext>" +
					"    <version>v2c</version>" +
					"    <specific>3</specific>" +
					"    <generic>6</generic>" +
					"    <community>public</community>" +
					"  </snmp>" +
					"  <descr>descr</descr>" +
					"  <logmsg>log message</logmsg>" +
					"  <severity>normal</severity>" +
					"</event>",
				"target/classes/xsds/eventconf.xsd" }, 
		});
	}

}
