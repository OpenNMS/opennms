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
package org.opennms.netmgt.config.datacollection;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class SystemsTest extends XmlTestNoCastor<Systems> {

    public SystemsTest(final Systems sampleObject, final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }
    
    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final Systems systems = new Systems();

        final SystemDef riverbed = new SystemDef();
        riverbed.setName("Riverbed Steelhead WAN Accelerators");
        riverbed.setSysoid(".1.3.6.1.4.1.17163.1.1");
        final Collect riverbedCollect = new Collect();
        riverbedCollect.addIncludeGroup("mib2-X-interfaces");
        riverbedCollect.addIncludeGroup("riverbed-steelhead-scalars");
        riverbedCollect.addIncludeGroup("riverbed-steelhead-cpu-stats");
        riverbedCollect.addIncludeGroup("riverbed-steelhead-port-bandwidth");
        riverbed.setCollect(riverbedCollect);

        final SystemDef lexmark = new SystemDef();
        lexmark.setName("Lexmark / Dell Printers");
        lexmark.setSysoidMask(".1.3.6.1.4.1.641.");
        final Collect lexmarkCollect = new Collect();
        lexmarkCollect.addIncludeGroup("printer-usage");
        lexmarkCollect.addIncludeGroup("printer-mib-supplies");
        lexmark.setCollect(lexmarkCollect);
        
        systems.addSystemDef(riverbed);
        
        return Arrays.asList(new Object[][] {
            {
                systems,
                "<systems>\n" +
                "      <systemDef name=\"Riverbed Steelhead WAN Accelerators\">\n" + 
                "        <sysoid>.1.3.6.1.4.1.17163.1.1</sysoid>\n" + 
                "        <collect>\n" + 
                "          <includeGroup>mib2-X-interfaces</includeGroup>\n" + 
                "          <includeGroup>riverbed-steelhead-scalars</includeGroup>\n" + 
                "          <includeGroup>riverbed-steelhead-cpu-stats</includeGroup>\n" + 
                "          <includeGroup>riverbed-steelhead-port-bandwidth</includeGroup>\n" + 
                "        </collect>\n" + 
                "      </systemDef>\n" +
                "</systems>\n",
                "target/classes/xsds/datacollection-config.xsd"
            }
        });
    }


}
