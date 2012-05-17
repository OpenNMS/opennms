package org.opennms.netmgt.xml.eventconf;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class AutoacknowledgeTest extends XmlTestNoCastor<Autoacknowledge> {

	public AutoacknowledgeTest(final Autoacknowledge sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		Autoacknowledge autoacknowledge0 = new Autoacknowledge();
		Autoacknowledge autoacknowledge1 = new Autoacknowledge();
		autoacknowledge1.setContent("These are important data");
		autoacknowledge1.setState("on");
		return Arrays.asList(new Object[][] {
				{autoacknowledge0,
				"<autoacknowledge/>",
				"target/classes/xsds/eventconf.xsd" },
				{autoacknowledge1,
				"<autoacknowledge state=\"on\">These are important data</autoacknowledge>",
				"target/classes/xsds/eventconf.xsd" } 
		});
	}

}
