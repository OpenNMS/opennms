package org.opennms.netmgt.xml.eventconf;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class AutoactionTest extends XmlTestNoCastor<Autoaction> {

	public AutoactionTest(final Autoaction sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		Autoaction autoaction0 = new Autoaction();
		Autoaction autoaction1 = new Autoaction();
		autoaction1.setContent("These are important data");
		autoaction1.setState("on");
		return Arrays.asList(new Object[][] {
				{autoaction0,
				"<autoaction/>",
				"target/classes/xsds/eventconf.xsd" },
				{autoaction1,
				"<autoaction state=\"on\">These are important data</autoaction>",
				"target/classes/xsds/eventconf.xsd" } 
		});
	}

}
