package org.opennms.netmgt.xml.eventconf;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class CorrelationTest extends XmlTestNoCastor<Correlation> {

	public CorrelationTest(final Correlation sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		Correlation correlation0 = new Correlation();
		Correlation correlation1 = new Correlation();
		correlation1.setState("on");
		correlation1.setPath("pathOutage");
		correlation1.setCmin("cmin");
		correlation1.setCmax("cmax");
		correlation1.setCtime("ctime");
		correlation1.addCuei("vCuei");
		return Arrays.asList(new Object[][] {
				{correlation0,
				"<correlation/>",
				"target/classes/xsds/eventconf.xsd" },
				{correlation1,
				"<correlation state=\"on\" path=\"pathOutage\">" +
				"<cuei>vCuei</cuei>" +
				"<cmin>cmin</cmin>" +
				"<cmax>cmax</cmax>" +
				"<ctime>ctime</ctime>" +
				"</correlation>",
				"target/classes/xsds/eventconf.xsd" } 
		});
	}

}
