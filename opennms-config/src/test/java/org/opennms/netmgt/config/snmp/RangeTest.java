package org.opennms.netmgt.config.snmp;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTest;

public class RangeTest extends XmlTest<Range> {

	public RangeTest(final Range sampleObject, final String sampleXml,
			final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		return Arrays.asList(new Object[][] { {
				new Range("192.168.1.1", "192.168.1.254"),
				"<range begin=\"192.168.1.1\" end=\"192.168.1.254\" />",
				"target/classes/xsds/snmp-config.xsd" } });
	}

}
