package org.opennms.netmgt.config.snmp;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTest;

public class SnmpConfigTest extends XmlTest<SnmpConfig> {

	public SnmpConfigTest(final SnmpConfig sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		final List<Definition> definitionList = new ArrayList<Definition>();
		
		final Range range = new Range();
		range.setBegin("192.168.0.1");
		range.setEnd("192.168.0.255");

		final Definition def = new Definition();
		def.setVersion("v3");
		def.setReadCommunity("public");
		def.setWriteCommunity("private");
		def.addRange(range);
		def.addSpecific("192.168.1.1");
		def.addIpMatch("10.0.0.*");
		definitionList.add(def);

		return Arrays.asList(new Object[][] {
				{
					new SnmpConfig(
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
							"enterpriseId",
							definitionList
					),
					"<snmp-config " +
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
					"  enterprise-id=\"enterpriseId\">" +
					"  <definition " +
					"    read-community=\"public\" " +
					"    write-community=\"private\" " +
					"    version=\"v3\">" +
					"    <range " +
					"      begin=\"192.168.0.1\" " +
					"      end=\"192.168.0.255\"/>" +
					"    <specific>192.168.1.1</specific>" +
					"    <ip-match>10.0.0.*</ip-match>" +
					"  </definition>" +
					"</snmp-config>\n",
					"target/classes/xsds/snmp-config.xsd"
				},
				{
					new SnmpConfig(
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
							"enterpriseId",
							definitionList
					),
					"<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" " +
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
					"  enterprise-id=\"enterpriseId\">" +
					"  <definition " +
					"    read-community=\"public\" " +
					"    write-community=\"private\" " +
					"    version=\"v3\">" +
					"    <range " +
					"      begin=\"192.168.0.1\" " +
					"      end=\"192.168.0.255\"/>" +
					"    <specific>192.168.1.1</specific>" +
					"    <ip-match>10.0.0.*</ip-match>" +
					"  </definition>" +
					"</snmp-config>\n",
					"target/classes/xsds/snmp-config.xsd"
				},
				
		});
	}

}
