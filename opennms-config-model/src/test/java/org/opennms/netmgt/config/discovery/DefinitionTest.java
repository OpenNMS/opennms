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
package org.opennms.netmgt.config.discovery;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class DefinitionTest extends XmlTestNoCastor<Definition> {
    public DefinitionTest(Definition sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/discovery-configuration.xsd");
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws ParseException {


        final IncludeRange ten = new IncludeRange("10.0.0.1", "10.0.0.254");
        ten.setLocation("here");
        ten.setRetries(2);

        final Specific specific = new Specific();
        specific.setAddress("10.0.0.3");
        specific.setLocation("minion");

        final ExcludeRange excludeRange = new ExcludeRange();
        excludeRange.setBegin("10.0.0.12");
        excludeRange.setEnd("10.0.0.22");

        // Add definition.
        Definition definition = new Definition();
        Detector detector = new Detector();
        detector.setName("icmp");
        detector.setClassName("org.opennms.icmp.IcmpDetector");
        Parameter parameter = new Parameter();
        parameter.setKey("port");
        parameter.setValue("8980");
        detector.addParamter(parameter);
        definition.addDetector(detector);
        definition.setLocation("Minion");
        definition.addSpecific(specific);
        definition.addIncludeRange(ten);
        definition.addExcludeRange(excludeRange);
        definition.setRetries(1);
        definition.setTimeout(2000L);
        definition.setForeignSource("OpenNMS-HQ");


        return Arrays.asList(new Object[][]{
                {
                        definition,
                        "   <definition location=\"Minion\" retries=\"1\" timeout=\"2000\" foreign-source=\"OpenNMS-HQ\">\n" +
                                "     <detectors>" +
                                "       <detector name=\"icmp\" class-name=\"org.opennms.icmp.IcmpDetector\">\n" +
                                "         <parameter key=\"port\" value=\"8980\" />\n" +
                                "       </detector>" +
                                "     </detectors>" +
                                "    <specific location=\"minion\">10.0.0.3</specific>" +
                                "    <include-range location=\"here\" retries=\"2\">\n" +
                                "        <begin>10.0.0.1</begin>\n" +
                                "        <end>10.0.0.254</end>\n" +
                                "    </include-range>\n" +
                                "    <exclude-range>" +
                                "      <begin>10.0.0.12</begin>\n" +
                                "      <end>10.0.0.22</end>\n" +
                                "    </exclude-range>" +
                                "    </definition>"
                }
        });
    }
}
