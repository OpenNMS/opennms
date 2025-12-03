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

public class VarbindTest extends XmlTestNoCastor<Varbind> {

	public VarbindTest(final Varbind sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		Varbind varbind0 = new Varbind();
		varbind0.setVbnumber(5);
		varbind0.addVbvalue("0");
		Varbind varbind1 = new Varbind();
		varbind1.setVbnumber(5);
		varbind1.addVbvalue("0");
		varbind1.setTextualConvention("MacAddress");
        Varbind varbind2 = new Varbind();
        varbind2.setVboid(".1.2.3");
        varbind2.addVbvalue("0");
		return Arrays.asList(new Object[][] {
				{varbind0,
				"<varbind>" +
				"<vbnumber>5</vbnumber>" +
				"<vbvalue>0</vbvalue>" +
				"</varbind>",
				"target/classes/xsds/eventconf.xsd" }, 
				{varbind1,
					"<varbind textual-convention=\"MacAddress\">" +
					"<vbnumber>5</vbnumber>" +
					"<vbvalue>0</vbvalue>" +
					"</varbind>",
					"target/classes/xsds/eventconf.xsd" },
                {varbind2,
                    "<varbind>" +
                    "<vboid>.1.2.3</vboid>" +
                    "<vbvalue>0</vbvalue>" +
                    "</varbind>",
                    "target/classes/xsds/eventconf.xsd" }
		});
	}
}
