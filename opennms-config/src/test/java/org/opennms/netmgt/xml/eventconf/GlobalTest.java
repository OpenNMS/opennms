package org.opennms.netmgt.xml.eventconf;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class GlobalTest extends XmlTestNoCastor<Global> {

	public GlobalTest(final Global sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		Global global0 = new Global();
		Security security0 = new Security();
		security0.addDoNotOverride("I'm very important, don't mess with me!");
		global0.setSecurity(security0);
		Global global1 = new Global();
		Security security1 = new Security();
		security1.addDoNotOverride("I'm very important, don't mess with me!");
		security1.addDoNotOverride("Also important");
		global1.setSecurity(security1);
		return Arrays.asList(new Object[][] {
				{global0,
				"<global>" +
				"<security>" +
				"<doNotOverride>I'm very important, don't mess with me!</doNotOverride>" +
				"</security>" +
				"</global>",
				"target/classes/xsds/eventconf.xsd" }, 
				{global1,
					"<global>" +
					"<security>" +
					"<doNotOverride>I'm very important, don't mess with me!</doNotOverride>" +
					"<doNotOverride>Also important</doNotOverride>" +
					"</security>" +
					"</global>",
					"target/classes/xsds/eventconf.xsd" }, 
		});
	}

}
