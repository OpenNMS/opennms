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
package org.opennms.netmgt.xml.eventconf;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class SnmpTest extends XmlTestNoCastor<Snmp> {

	public SnmpTest(final Snmp sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		Snmp snmp0 = new Snmp();
		snmp0.setId(".1.3.6.1.4.1.9");
		snmp0.setVersion("v2c");
		Snmp snmp1 = new Snmp();
		snmp1.setId(".1.3.6.1.4.1.9");
		snmp1.setVersion("v2c");
		snmp1.setGeneric(6);
		snmp1.setSpecific(3);
		snmp1.setIdtext("Test");
		snmp1.setCommunity("public");
		return Arrays.asList(new Object[][] {
				{snmp0,
				"<snmp>" +
				"<id>.1.3.6.1.4.1.9</id>" +
				"<version>v2c</version>" +
				"</snmp>",
				"target/classes/xsds/eventconf.xsd" }, 
				{snmp1,
				"<snmp>" +
				"<id>.1.3.6.1.4.1.9</id>" +
				"<idtext>Test</idtext>" +
				"<version>v2c</version>" +
				"<specific>3</specific>" +
				"<generic>6</generic>" +
				"<community>public</community>" +
				"</snmp>",
				"target/classes/xsds/eventconf.xsd" }, 
		});
	}

}
