package org.opennms.netmgt.xml.eventconf;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class VarbindsdecodeTest extends XmlTestNoCastor<Varbindsdecode> {
	
	public VarbindsdecodeTest(final Varbindsdecode sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		Varbindsdecode varbindsdecode0 = new Varbindsdecode();
		Decode decode0 = new Decode();
		decode0.setVarbinddecodedstring("testing");
		decode0.setVarbindvalue("3");
		varbindsdecode0.addDecode(decode0);
		varbindsdecode0.setParmid("parm[#1]");
		return Arrays.asList(new Object[][] {
				{varbindsdecode0,
				"<varbindsdecode>" +
				"<parmid>parm[#1]</parmid>" +
				"<decode varbinddecodedstring=\"testing\" varbindvalue=\"3\"/>" +
				"</varbindsdecode>",
				"target/classes/xsds/eventconf.xsd" }, 
		});
	}

}
