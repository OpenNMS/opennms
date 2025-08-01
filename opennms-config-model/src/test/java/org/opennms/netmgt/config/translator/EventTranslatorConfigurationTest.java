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
package org.opennms.netmgt.config.translator;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class EventTranslatorConfigurationTest extends XmlTestNoCastor<EventTranslatorConfiguration> {

    public EventTranslatorConfigurationTest(EventTranslatorConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/translator-configuration.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getConfig(),
                "<event-translator-configuration\n" + 
                "xmlns=\"http://xmlns.opennms.org/xsd/translator-configuration\"\n" + 
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >\n" + 
                "  <translation>\n" + 
                "    <event-translation-spec uei=\"uei.opennms.org/mib2opennms/tspEventPCRRepetitionError\">\n" + 
                "      <!-- Each event can have 1 or more mappings. -->\n" + 
                "      <mappings>\n" + 
                "        <mapping>\n" + 
                "          <!-- This mapping uses the SQL value type to query the DB and change the nodeid of the new event -->\n" + 
                "          <assignment type=\"field\" name=\"nodeid\">\n" + 
                "            <value type=\"sql\" result=\"select node.nodeid from node, ipInterface where node.nodeLabel=? and ipinterface.ipaddr=? and node.nodeId=ipinterface.nodeid and ipInterface.isManaged != 'D' and node.nodeType != 'D'\" >\n" + 
                "              <!-- These are sub value types that are used as parameters to the above sql as in JDBC speak -->\n" + 
                "              <value type=\"parameter\" name=\".1.3.6.1.4.1.6768.6.2.2.5.0\" matches=\"^([A-z]+) ([0-9]+).*\" result=\"${1}-${2}\" />\n" + 
                "              <value type=\"constant\" result=\"169.254.1.1\" />\n" + 
                "            </value>\n" + 
                "          </assignment>\n" + 
                "        </mapping>\n" + 
                "      </mappings>\n" + 
                "    </event-translation-spec>\n" + 
                "  </translation>\n" + 
                "</event-translator-configuration>"
            },
            {
                new EventTranslatorConfiguration(),
                "<event-translator-configuration><translation/></event-translator-configuration>"
            }
        });
    }

    private static EventTranslatorConfiguration getConfig() {
        EventTranslatorConfiguration config = new EventTranslatorConfiguration();
        
        EventTranslationSpec spec = new EventTranslationSpec();
        spec.setUei("uei.opennms.org/mib2opennms/tspEventPCRRepetitionError");
        config.addEventTranslationSpec(spec);

        Mapping mapping = new Mapping();
        spec.addMapping(mapping);

        Assignment assignment = new Assignment();
        assignment.setType("field");
        assignment.setName("nodeid");
        mapping.addAssignment(assignment);

        Value v1 = new Value();
        v1.setType("sql");
        v1.setResult("select node.nodeid from node, ipInterface where node.nodeLabel=? and ipinterface.ipaddr=? and node.nodeId=ipinterface.nodeid and ipInterface.isManaged != 'D' and node.nodeType != 'D'");
        assignment.setValue(v1);

        Value v2 = new Value();
        v2.setType("parameter");
        v2.setName(".1.3.6.1.4.1.6768.6.2.2.5.0");
        v2.setMatches("^([A-z]+) ([0-9]+).*");
        v2.setResult("${1}-${2}");
        v1.addValue(v2);

        Value v3 = new Value();
        v3.setType("constant");
        v3.setResult("169.254.1.1");
        v1.addValue(v3);

        return config;
    }
}
