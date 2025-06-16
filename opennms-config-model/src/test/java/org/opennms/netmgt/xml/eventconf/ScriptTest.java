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

public class ScriptTest extends XmlTestNoCastor<Script> {

	public ScriptTest(final Script sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		Script script0 = new Script();
		script0.setLanguage("erlang");
		Script script1 = new Script();
		script1.setLanguage("erlang");
		script1.setContent("This is a test");
		return Arrays.asList(new Object[][] {
				{script0,
				"<script language=\"erlang\"/>",
				"target/classes/xsds/eventconf.xsd" }, 
				{script1,
					"<script language=\"erlang\">This is a test</script>",
					"target/classes/xsds/eventconf.xsd" }, 
		});
	}

}
