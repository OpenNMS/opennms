package org.opennms.netmgt.xml.eventconf;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class SecurityTest extends XmlTestNoCastor<Security> {

	public SecurityTest(final Security sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		Security security0 = new Security();
		security0.addDoNotOverride("I'm very important, don't mess with me!");
		Security security1 = new Security();
		security1.addDoNotOverride("I'm very important, don't mess with me!");
		security1.addDoNotOverride("Also important");
		return Arrays.asList(new Object[][] {
				{security0,
				"<security>" +
				"<doNotOverride>I'm very important, don't mess with me!</doNotOverride>" +
				"</security>",
				"target/classes/xsds/eventconf.xsd" }, 
				{security1,
					"<security>" +
					"<doNotOverride>I'm very important, don't mess with me!</doNotOverride>" +
					"<doNotOverride>Also important</doNotOverride>" +
					"</security>",
					"target/classes/xsds/eventconf.xsd" }, 		});
	}

}
