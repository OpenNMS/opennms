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
		Snmpv3User userA = new Snmpv3User();
		userA.setAuthPassphrase("0p3nNMSv3");
		userA.setAuthProtocol("MD5");
		userA.setPrivacyPassphrase("0p3nNMSv3");
		userA.setPrivacyProtocol("DES");
		userA.setSecurityName("opennms");

		Snmpv3User userB = new Snmpv3User();
		userB.setAuthPassphrase("0p3nNMSv3");
		userB.setAuthProtocol("MD5");
		userB.setPrivacyPassphrase("0p3nNMSv3");
		userB.setPrivacyProtocol("DES");
		userB.setSecurityName("opennms2");

		Snmpv3User userC = new Snmpv3User();

		TrapdConfiguration configWithSnmpv3User = new TrapdConfiguration(162,"*");
		configWithSnmpv3User.addSnmpv3User(userA);

		TrapdConfiguration configWithSnmpv3Users = new TrapdConfiguration(162,"*");
		configWithSnmpv3Users.addSnmpv3User(userA);
		configWithSnmpv3Users.addSnmpv3User(userB);

		// To make sure that optional fields are omitted
		TrapdConfiguration configWithEmptyUser = new TrapdConfiguration(162,"*");
		configWithEmptyUser.addSnmpv3User(userC);

		return Arrays.asList(new Object[][] {
			{
				new TrapdConfiguration(162,"*"),
				"<trapd-configuration "
					+ "snmp-trap-address=\"*\" "
					+ "snmp-trap-port=\"162\" "
					+ "new-suspect-on-trap=\"false\" "
					+ "/>",
				"target/classes/xsds/trapd-configuration.xsd"
			},
			{
				new TrapdConfiguration(162,"*"),
				"<trapd-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/trapd\" "
					+ "snmp-trap-address=\"*\" "
					+ "snmp-trap-port=\"162\" "
					+ "new-suspect-on-trap=\"false\" "
					+ "/>",
				"target/classes/xsds/trapd-configuration.xsd"
			},
			{
				configWithSnmpv3User,
				"<trapd-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/trapd\" "
					+ "snmp-trap-address=\"*\" "
					+ "snmp-trap-port=\"162\" "
					+ "new-suspect-on-trap=\"false\" "
					+ ">"
					+   "<snmpv3-user security-name=\"opennms\" auth-passphrase=\"0p3nNMSv3\" auth-protocol=\"MD5\" privacy-passphrase=\"0p3nNMSv3\" privacy-protocol=\"DES\"/>"
					+ "</trapd-configuration>",
				"target/classes/xsds/trapd-configuration.xsd"
			},
			{
				configWithSnmpv3Users,
				"<trapd-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/trapd\" "
					+ "snmp-trap-address=\"*\" "
					+ "snmp-trap-port=\"162\" "
					+ "new-suspect-on-trap=\"false\" "
					+ ">"
					+   "<snmpv3-user security-name=\"opennms\" auth-passphrase=\"0p3nNMSv3\" auth-protocol=\"MD5\" privacy-passphrase=\"0p3nNMSv3\" privacy-protocol=\"DES\"/>"
					+   "<snmpv3-user security-name=\"opennms2\" auth-passphrase=\"0p3nNMSv3\" auth-protocol=\"MD5\" privacy-passphrase=\"0p3nNMSv3\" privacy-protocol=\"DES\"/>"
					+ "</trapd-configuration>",
				"target/classes/xsds/trapd-configuration.xsd"
			},
			{
				configWithEmptyUser,
				"<trapd-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/trapd\" "
					+ "snmp-trap-address=\"*\" "
					+ "snmp-trap-port=\"162\" "
					+ "new-suspect-on-trap=\"false\" "
					+ ">"
					+   "<snmpv3-user />"
					+ "</trapd-configuration>",
				"target/classes/xsds/trapd-configuration.xsd"
			}
		});

	}

}
