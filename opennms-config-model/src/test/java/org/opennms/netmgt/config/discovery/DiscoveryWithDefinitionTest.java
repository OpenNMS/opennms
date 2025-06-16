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

public class DiscoveryWithDefinitionTest extends XmlTestNoCastor<DiscoveryConfiguration> {

    public DiscoveryWithDefinitionTest(DiscoveryConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/discovery-configuration.xsd");
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws ParseException {

        final IncludeRange ten = new IncludeRange("10.0.0.1", "10.0.0.254");
        ten.setLocation("here");
        ten.setRetries(2);

        final DiscoveryConfiguration discoveryConf = new DiscoveryConfiguration();
        discoveryConf.setPacketsPerSecond(1d);
        discoveryConf.setInitialSleepTime(30000l);
        discoveryConf.setRestartSleepTime(86400000l);
        discoveryConf.setRetries(1);
        discoveryConf.setTimeout(2000l);

        final IncludeRange exampleRange = new IncludeRange("192.168.0.1", "192.168.0.254");
        discoveryConf.addIncludeRange(exampleRange);

        discoveryConf.addIncludeUrl(new IncludeUrl("file:/opt/opennms/etc/include.txt"));
        discoveryConf.addIncludeUrl(new IncludeUrl("http://example.com/ip-address-list.txt"));

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
        definition.addIncludeRange(ten);
        definition.addExcludeRange(new ExcludeRange("192.168.2.1", "192.168.2.254"));
        definition.addIncludeUrl(new IncludeUrl("http://example.com/ip-address-list.txt"));
        definition.setLocation("Minion");
        discoveryConf.addDefinition(definition);

        return Arrays.asList(new Object[][] {
                {
                        discoveryConf,
                        "<discovery-configuration packets-per-second=\"1.0\"\n" +
                                "        initial-sleep-time=\"30000\" restart-sleep-time=\"86400000\"\n" +
                                "        retries=\"1\" timeout=\"2000\">\n" +
                                "        <include-range>\n" +
                                "                <begin>192.168.0.1</begin>\n" +
                                "                <end>192.168.0.254</end>\n" +
                                "        </include-range>\n" +
                                "   <include-url>file:/opt/opennms/etc/include.txt</include-url>\n" +
                                "   <include-url>http://example.com/ip-address-list.txt</include-url>\n" +
                                "   <definition location=\"Minion\">\n" +
                                "     <detectors>" +
                                "        <detector name=\"icmp\" class-name=\"org.opennms.icmp.IcmpDetector\">\n" +
                                "          <parameter key=\"port\" value=\"8980\" />\n" +
                                "        </detector>" +
                                "      </detectors>" +
                                "      <include-range location=\"here\" retries=\"2\">\n" +
                                "          <begin>10.0.0.1</begin>\n" +
                                "          <end>10.0.0.254</end>\n" +
                                "      </include-range>\n" +
                                "      <exclude-range>\n" +
                                "          <begin>192.168.2.1</begin>\n" +
                                "          <end>192.168.2.254</end>\n" +
                                "      </exclude-range>\n" +
                                "      <include-url>http://example.com/ip-address-list.txt</include-url>\n" +
                                "    </definition>" +
                                "</discovery-configuration>"
                }
        });
    }
}
