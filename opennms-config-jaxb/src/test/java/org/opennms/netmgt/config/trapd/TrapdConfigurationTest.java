package org.opennms.netmgt.config.trapd;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class TrapdConfigurationTest extends XmlTestNoCastor<TrapdConfiguration> {

	public TrapdConfigurationTest(final TrapdConfiguration sampleObject,
			final Object sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		return Arrays
				.asList(new Object[][] {
						{
								new TrapdConfiguration(162,"*"),
								"<trapd-configuration"
										+ "  snmp-trap-address=\"*\"  "
										+ "  snmp-trap-port=\"162\"  "
										+ "  new-suspect-on-trap=\"false\" "
										+ "/>",
								"target/classes/xsds/trapd-configuration.xsd" },
						{
								new TrapdConfiguration(162,"*"),
								"<trapd-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/trapd\" "
										+ "  snmp-trap-address=\"*\"  "
										+ "  snmp-trap-port=\"162\"  "
										+ "  new-suspect-on-trap=\"false\" "
										+ "/>",
								"target/classes/xsds/trapd-configuration.xsd" } });

	}

}
