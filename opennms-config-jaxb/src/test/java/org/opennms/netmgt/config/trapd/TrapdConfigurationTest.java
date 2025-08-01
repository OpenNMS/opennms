/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.config.trapd;

import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.core.xml.JaxbUtils;

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
						"target/classes/xsds/trapd-configuration.xsd"
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
				"target/classes/xsds/trapd-configuration.xsd"
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
				"target/classes/xsds/trapd-configuration.xsd"
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
				"target/classes/xsds/trapd-configuration.xsd"
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
				"target/classes/xsds/trapd-configuration.xsd"
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
				"target/classes/xsds/trapd-configuration.xsd"
			}
		});

	}
	
	  /**  Try to validate missing "required" fields and misspellings in "optional" fields **/
	  @Test
	  public void validateUsingJaxbUtils() {
	        
        String validConfig = "<trapd-configuration " 
                                 + "snmp-trap-port=\"1111\" " 
                                 + "new-suspect-on-trap=\"false\" "
                                 + "/>";

        try {
            JaxbUtils.unmarshal(TrapdConfiguration.class, validConfig);
        } catch (Exception e) {
            fail();
        }
        String missingPortConfig = "<trapd-configuration "
                                       + "new-suspect-on-trap=\"false\" " 
                                       + "/>";

        try {
            JaxbUtils.unmarshal(TrapdConfiguration.class, missingPortConfig);
            fail();
        } catch (Exception e) {
        }
        String missingNewSuspectOnTrapConfig = "<trapd-configuration " 
                                                   + "snmp-trap-port=\"1111\" "
                                                   + "/>";
        try {
            JaxbUtils.unmarshal(TrapdConfiguration.class, missingNewSuspectOnTrapConfig);
            fail();
        } catch (Exception e) {
        }

        String misspelledConfig = "<trapd-configuration "
                                      + "Ssnmp-trap-port=\"1111\" "
                                      + "new-suspect-on-trap=\"false\" " 
                                      + "/>";

        try {
            JaxbUtils.unmarshal(TrapdConfiguration.class, misspelledConfig);
            fail();
        } catch (Exception e) {
        }

        String missplledConfig1 = "<trapd-configuration " 
                                      + "snmp-crap-address=\"*\" " 
                                      + "snmp-trap-port=\"162\" "
                                      + "new-suspect-on-trap=\"false\" " 
                                      + "include-raw-message=\"false\" "
                                      + "threads=\"0\" "
                                      + "queue-size=\"10000\" " 
                                      + "batch-size=\"1000\" " 
                                      + "batch-interval=\"500\"" + "/>";
        try {
            JaxbUtils.unmarshal(TrapdConfiguration.class, missplledConfig1);
            fail();
        } catch (Exception e) {
        }
	        
	    }

}
