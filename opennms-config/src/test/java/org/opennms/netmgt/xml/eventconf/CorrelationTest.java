package org.opennms.netmgt.xml.eventconf;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTest;

public class CorrelationTest extends XmlTest<Correlation> {

	public CorrelationTest(final Correlation sampleObject, final String sampleXml,
			final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		Correlation correlation0 = new Correlation();
		Correlation correlation1 = new Correlation();
		correlation1.setState("on");
		return Arrays.asList(new Object[][] {
				{correlation0,
				"<correlation/>",
				"target/classes/xsds/eventconf.xsd" },
//				{correlation1,
//				"<correlation state=\"on\">These are important data</correlation>",
//				"target/classes/xsds/eventconf.xsd" } 
		});
	}

}
