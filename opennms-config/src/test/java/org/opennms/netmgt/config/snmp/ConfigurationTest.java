package org.opennms.netmgt.config.snmp;


import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTest;

public class ConfigurationTest extends XmlTest<Configuration> {

	public ConfigurationTest(final Configuration sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		return Arrays.asList(new Object[][] {
				{
					new Configuration(
							1, // port
							2, // retry
							3, // timeout
							"readCommunity",
							"writeCommunity",
							"proxyHost",
							"v2c", // version
							4, // max-vars-per-pdu
							5, // max-repetitions
							484, // max-request-size
							"securityName",
							3, // security-level
							"authPassphrase",
							"MD5", //auth-protocol
							"engineId",
							"contextEngineId",
							"contextName",
							"privacyPassphrase",
							"DES", // privacy-protocol
							"enterpriseId"
					),
					"<configuration " +
					"  port=\"1\" " +
					"  retry=\"2\" " +
					"  timeout=\"3\" " +
					"  read-community=\"readCommunity\" " +
					"  write-community=\"writeCommunity\" " +
					"  proxy-host=\"proxyHost\" " +
					"  version=\"v2c\" " +
					"  max-vars-per-pdu=\"4\" " +
					"  max-repetitions=\"5\" " +
					"  max-request-size=\"484\" " +
					"  security-name=\"securityName\" " +
					"  security-level=\"3\" " +
					"  auth-passphrase=\"authPassphrase\" " +
					"  auth-protocol=\"MD5\" " +
					"  engine-id=\"contextEngineId\" " +
					"  context-engine-id=\"contextEngineId\" " +
					"  context-name=\"contextName\" " +
					"  privacy-passphrase=\"privacyPassphrase\" " +
					"  privacy-protocol=\"DES\" " +
					"  enterprise-id=\"enterpriseId\" />\n",
					"src/main/resources/xsds/snmp-config.xsd"
				}
		});
	}
}
