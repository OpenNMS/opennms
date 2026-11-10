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
package org.opennms.netmgt.config.snmp;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class DefinitionTest extends XmlTestNoCastor<Definition> {

	public DefinitionTest(final Definition sampleObject,
			final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
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
		def.setLocation("MINION");
		def.setProfileLabel("label1");

		return Arrays.asList(new Object[][] { {
				def,
				"  <definition "
				+ "    read-community=\"public\" "
				+ "    write-community=\"private\" "
				+ "    location=\"MINION\""
		        + "    profile-label=\"label1\""
				+ "    version=\"v3\">" + "    <range "
				+ "      begin=\"192.168.0.1\" "
				+ "      end=\"192.168.0.255\"/>"
				+ "    <specific>192.168.1.1</specific>"
				+ "    <ip-match>10.0.0.*</ip-match>"
				+ "  </definition>\n",
				"target/classes/xsds/snmp-config.xsd" }, });
	}

}
