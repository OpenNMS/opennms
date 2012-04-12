package org.opennms.netmgt.xml.eventconf;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class OperactionTest extends XmlTestNoCastor<Operaction> {

	public OperactionTest(final Operaction sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		Operaction operaction0 = new Operaction();
		operaction0.setMenutext("Test");
		Operaction operaction1 = new Operaction();
		operaction1.setMenutext("Test");
		operaction1.setContent("This is a test");
		operaction1.setState("on");
		return Arrays.asList(new Object[][] {
				{operaction0,
				"<operaction menutext=\"Test\"></operaction>",
				"target/classes/xsds/eventconf.xsd" }, 
				{operaction1,
					"<operaction menutext=\"Test\" state=\"on\">This is a test</operaction>",
					"target/classes/xsds/eventconf.xsd" }, 
		});
	}

}
