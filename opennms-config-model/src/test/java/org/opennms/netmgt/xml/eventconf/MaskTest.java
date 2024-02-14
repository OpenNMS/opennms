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

public class MaskTest extends XmlTestNoCastor<Mask> {

	public MaskTest(final Mask sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		Mask mask0 = new Mask();
		Maskelement maskelement = new Maskelement();
		maskelement.setMename("specific");
		maskelement.addMevalue("3");
		mask0.addMaskelement(maskelement);
		Mask mask1 = new Mask();
		mask1.addMaskelement(maskelement);
		Varbind varbind = new Varbind();
		varbind.setVbnumber(5);
		varbind.addVbvalue("0");
		mask1.addVarbind(varbind);
		return Arrays.asList(new Object[][] {
				{mask0,
				"<mask><maskelement><mename>specific</mename><mevalue>3</mevalue></maskelement></mask>",
				"target/classes/xsds/eventconf.xsd" }, 
				{mask1,
					" <mask> " +
					"<maskelement>" +
					"<mename>specific</mename>" +
					"<mevalue>3</mevalue>" +
					"</maskelement>" +
					"<varbind>" +
					"<vbnumber>5</vbnumber>" +
					"<vbvalue>0</vbvalue>" +
					"</varbind>" +
					"</mask>",
					"target/classes/xsds/eventconf.xsd" } 
		});
	}

}
