/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.config.convert;

import com.atlassian.oai.validator.report.ValidationReport;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.dao.api.ConfigConverter;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.netmgt.config.trapd.Snmpv3User;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.fail;

public class TrapdConfigurationTest extends CmConfigTest<TrapdConfiguration> {

	public TrapdConfigurationTest(final TrapdConfiguration sampleObject,
			final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile, "trapd-configuration");
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

		// With the new sink pattern (Release 19.0.0) new properties have been introduced. Add tests for them as well
		TrapdConfiguration configWithAllCustomTrapdProperties = new TrapdConfiguration(1111, "192.193.194.195");
		configWithAllCustomTrapdProperties.setIncludeRawMessage(true);
		configWithAllCustomTrapdProperties.setThreads(10);
		configWithAllCustomTrapdProperties.setBatchSize(1);
		configWithAllCustomTrapdProperties.setQueueSize(2);
		configWithAllCustomTrapdProperties.setNewSuspectOnTrap(true);
		configWithAllCustomTrapdProperties.setBatchInterval(0);

		return Arrays.asList(new Object[][] {
				{
						new TrapdConfiguration(162,"*"),
						"<trapd-configuration "
								+ "snmp-trap-address=\"*\" "
								+ "snmp-trap-port=\"162\" "
								+ "new-suspect-on-trap=\"false\" "
								+ "include-raw-message=\"false\" "
								+ "threads=\"0\" "
								+ "queue-size=\"10000\" "
								+ "batch-size=\"1000\" "
								+ "batch-interval=\"500\""
								+ "/>",
						"trapd-configuration.xsd"
				},
			{
				new TrapdConfiguration(162,"*"),
				"<trapd-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/trapd\" "
					+ "snmp-trap-address=\"*\" "
					+ "snmp-trap-port=\"162\" "
					+ "new-suspect-on-trap=\"false\" "
					+ "include-raw-message=\"false\" "
					+ "threads=\"0\" "
					+ "queue-size=\"10000\" "
					+ "batch-size=\"1000\" "
					+ "batch-interval=\"500\""
					+ "/>",
				"trapd-configuration.xsd"
			},
			{
				configWithSnmpv3User,
				"<trapd-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/trapd\" "
					+ "snmp-trap-address=\"*\" "
					+ "snmp-trap-port=\"162\" "
					+ "new-suspect-on-trap=\"false\" "
					+ "include-raw-message=\"false\" "
					+ "threads=\"0\" "
					+ "queue-size=\"10000\" "
					+ "batch-size=\"1000\" "
					+ "batch-interval=\"500\""
					+ ">"
					+   "<snmpv3-user security-name=\"opennms\" auth-passphrase=\"0p3nNMSv3\" auth-protocol=\"MD5\" privacy-passphrase=\"0p3nNMSv3\" privacy-protocol=\"DES\"/>"
					+ "</trapd-configuration>",
				"trapd-configuration.xsd"
			},
			{
				configWithSnmpv3Users,
				"<trapd-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/trapd\" "
					+ "snmp-trap-address=\"*\" "
					+ "snmp-trap-port=\"162\" "
					+ "new-suspect-on-trap=\"false\" "
					+ "include-raw-message=\"false\" "
					+ "threads=\"0\" "
					+ "queue-size=\"10000\" "
					+ "batch-size=\"1000\" "
					+ "batch-interval=\"500\""
					+ ">"
					+   "<snmpv3-user security-name=\"opennms\" auth-passphrase=\"0p3nNMSv3\" auth-protocol=\"MD5\" privacy-passphrase=\"0p3nNMSv3\" privacy-protocol=\"DES\"/>"
					+   "<snmpv3-user security-name=\"opennms2\" auth-passphrase=\"0p3nNMSv3\" auth-protocol=\"MD5\" privacy-passphrase=\"0p3nNMSv3\" privacy-protocol=\"DES\"/>"
					+ "</trapd-configuration>",
				"trapd-configuration.xsd"
			},
			{
				configWithEmptyUser,
				"<trapd-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/trapd\" "
					+ "snmp-trap-address=\"*\" "
					+ "snmp-trap-port=\"162\" "
					+ "new-suspect-on-trap=\"false\" "
					+ "include-raw-message=\"false\" "
					+ "threads=\"0\" "
					+ "queue-size=\"10000\" "
					+ "batch-size=\"1000\" "
					+ "batch-interval=\"500\""
					+ ">"
					+   "<snmpv3-user />"
					+ "</trapd-configuration>",
				"trapd-configuration.xsd"
			},
			{
				configWithAllCustomTrapdProperties,
				"<trapd-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/trapd\" "
						+ "snmp-trap-address=\"192.193.194.195\" "
						+ "snmp-trap-port=\"1111\" "
						+ "new-suspect-on-trap=\"true\" "
						+ "include-raw-message=\"true\" "
						+ "threads=\"10\" "
						+ "queue-size=\"2\" "
						+ "batch-size=\"1\" "
						+ "batch-interval=\"0\""
						+ "/>",
				"trapd-configuration.xsd"
			}
		});

	}
	
	  /**  Try to validate missing "required" fields and misspellings in "optional" fields **/
	  @Test
	  public void validateUsingJaxbUtils() {
        try {
			String validConfig = "<trapd-configuration "
					+ "snmp-trap-port=\"1111\" "
					+ "new-suspect-on-trap=\"false\" "
					+ "/>";
			ConfigConverter converter = XsdHelper.getConverter(configDefinition);
			String json = converter.xmlToJson(validConfig);
			ValidationReport report = configDefinition.validate(json);
			if(report.hasErrors()){
				fail();
			}
        } catch (Exception e) {
        }


        try {
			String missingPortConfig = "<trapd-configuration "
					+ "new-suspect-on-trap=\"false\" "
					+ "/>";
			ConfigConverter converter = XsdHelper.getConverter(configDefinition);
			String json = converter.xmlToJson(missingPortConfig);
			ValidationReport report = configDefinition.validate(json);
			if(!report.hasErrors()){
				fail();
			}
        } catch (Exception e) {
        }

        try {
			String missingNewSuspectOnTrapConfig = "<trapd-configuration "
					+ "snmp-trap-port=\"1111\" "
					+ "/>";
			ConfigConverter converter = XsdHelper.getConverter(configDefinition);
			String json = converter.xmlToJson(missingNewSuspectOnTrapConfig);
			ValidationReport report = configDefinition.validate(json);
			if(!report.hasErrors()){
				fail();
			}
        } catch (Exception e) {
        }



        try {
			String misspelledConfig = "<trapd-configuration "
					+ "Ssnmp-trap-port=\"1111\" "
					+ "new-suspect-on-trap=\"false\" "
					+ "/>";
			ConfigConverter converter = XsdHelper.getConverter(configDefinition);
			String json = converter.xmlToJson(misspelledConfig);
			ValidationReport report = configDefinition.validate(json);
			if(!report.hasErrors()){
				fail();
			}
        } catch (Exception e) {
        }

        //TODO: Freddy confirm is that acceptable to skip this test. Since xsd conversion will skip invalid config. (snmp-crap-address)
//        try {
//			String missplledConfig1 = "<trapd-configuration "
//					+ "snmp-crap-address=\"*\" "
//					+ "snmp-trap-port=\"162\" "
//					+ "new-suspect-on-trap=\"false\" "
//					+ "include-raw-message=\"false\" "
//					+ "threads=\"0\" "
//					+ "queue-size=\"10000\" "
//					+ "batch-size=\"1000\" "
//					+ "batch-interval=\"500\"" + "/>";
//			ConfigConverter converter = XsdHelper.getConverter(configDefinition);
//			String json = converter.xmlToJson(missplledConfig1);
//			ValidationReport report = configDefinition.validate(json);
//			if(!report.hasErrors()){
//				fail();
//			}
//        } catch (Exception e) {
//        }
	        
	    }

}
