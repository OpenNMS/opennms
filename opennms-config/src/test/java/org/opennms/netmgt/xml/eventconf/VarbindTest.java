package org.opennms.netmgt.xml.eventconf;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class VarbindTest extends XmlTestNoCastor<Varbind> {

	public VarbindTest(final Varbind sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		Varbind varbind0 = new Varbind();
		varbind0.setVbnumber(5);
		varbind0.addVbvalue("0");
		Varbind varbind1 = new Varbind();
		varbind1.setVbnumber(5);
		varbind1.addVbvalue("0");
		varbind1.setTextualConvention("MacAddress");
		return Arrays.asList(new Object[][] {
				{varbind0,
				"<varbind>" +
				"<vbnumber>5</vbnumber>" +
				"<vbvalue>0</vbvalue>" +
				"</varbind>",
				"target/classes/xsds/eventconf.xsd" }, 
				{varbind1,
					"<varbind textual-convention=\"MacAddress\">" +
					"<vbnumber>5</vbnumber>" +
					"<vbvalue>0</vbvalue>" +
					"</varbind>",
					"target/classes/xsds/eventconf.xsd" } 
		});
	}

}
