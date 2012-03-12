package org.opennms.netmgt.xml.eventconf;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class MaskTest extends XmlTestNoCastor<Mask> {

	public MaskTest(final Mask sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		Mask mask0 = new Mask();
		Maskelement maskelement = new Maskelement();
		maskelement.setMename("specific");
		maskelement.addMevalue("3");
		mask0.addMaskelement(maskelement);
		Mask mask1 = new Mask();
		mask1.addMaskelement(maskelement);
		Varbind varbind = new Varbind();
		varbind.setVbnumber(5);
		varbind.addVbvalue("0");
		mask1.addVarbind(varbind);
		return Arrays.asList(new Object[][] {
				{mask0,
				"<mask><maskelement><mename>specific</mename><mevalue>3</mevalue></maskelement></mask>",
				"target/classes/xsds/eventconf.xsd" }, 
				{mask1,
					" <mask> " +
					"<maskelement>" +
					"<mename>specific</mename>" +
					"<mevalue>3</mevalue>" +
					"</maskelement>" +
					"<varbind>" +
					"<vbnumber>5</vbnumber>" +
					"<vbvalue>0</vbvalue>" +
					"</varbind>" +
					"</mask>",
					"target/classes/xsds/eventconf.xsd" } 
		});
	}

}
