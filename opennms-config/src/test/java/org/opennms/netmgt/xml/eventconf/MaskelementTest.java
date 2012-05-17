package org.opennms.netmgt.xml.eventconf;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class MaskelementTest extends XmlTestNoCastor<Maskelement> {

	public MaskelementTest(final Maskelement sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		Maskelement maskelement0 = new Maskelement();
		maskelement0.setMename("specific");
		maskelement0.addMevalue("3");
		Maskelement maskelement1 = new Maskelement();
		maskelement1.setMename("specific");
		maskelement1.addMevalue("3");
		maskelement1.addMevalue("4");
		return Arrays.asList(new Object[][] {
				{maskelement0,
				"<maskelement> <mename>specific</mename> <mevalue>3</mevalue></maskelement>",
				"target/classes/xsds/eventconf.xsd" }, 
				{maskelement1,
					"<maskelement> <mename>specific</mename> <mevalue>3</mevalue><mevalue>4</mevalue></maskelement>",
					"target/classes/xsds/eventconf.xsd" }, 
		});
	}

}
