package org.opennms.netmgt.xml.eventconf;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class DecodeTest extends XmlTestNoCastor<Decode> {

	public DecodeTest(final Decode sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		Decode decode0 = new Decode();
		decode0.setVarbinddecodedstring("testing");
		decode0.setVarbindvalue("3");
		return Arrays.asList(new Object[][] {
				{decode0,
				"<decode varbinddecodedstring=\"testing\" varbindvalue=\"3\"/>",
				"target/classes/xsds/eventconf.xsd" } 
		});
	}

}
