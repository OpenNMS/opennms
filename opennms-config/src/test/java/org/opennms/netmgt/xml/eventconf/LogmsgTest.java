package org.opennms.netmgt.xml.eventconf;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class LogmsgTest extends XmlTestNoCastor<Logmsg> {

	public LogmsgTest(final Logmsg sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		Logmsg logmsg0 = new Logmsg();
		Logmsg logmsg1 = new Logmsg();
		logmsg1.setDest("logndisplay");
		logmsg1.setNotify(false);
		logmsg1.setContent("This is a test");
		return Arrays.asList(new Object[][] {
				{logmsg0,
				"<logmsg/>",
				"target/classes/xsds/eventconf.xsd" }, 
				{logmsg1,
					"<logmsg dest=\"logndisplay\" notify=\"false\">This is a test</logmsg>",
					"target/classes/xsds/eventconf.xsd" } 
		});
	}

}
