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

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class DiscoveryConfigurationTest extends XmlTestNoCastor<DiscoveryConfiguration> {

    public DiscoveryConfigurationTest(final DiscoveryConfiguration sampleObject, final Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/discovery-configuration.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final DiscoveryConfiguration conf = new DiscoveryConfiguration();
        conf.setPacketsPerSecond(1d);
        conf.setInitialSleepTime(30000l);
        conf.setRestartSleepTime(86400000l);
        conf.setRetries(1);
        conf.setTimeout(2000l);

        final IncludeRange oneNinetyTwo = new IncludeRange("192.168.0.1", "192.168.254.254");
        oneNinetyTwo.setTimeout(3000l);
        oneNinetyTwo.setForeignSource("blah");
        conf.addIncludeRange(oneNinetyTwo);

        final IncludeRange ten = new IncludeRange("10.0.0.1", "10.0.0.254");
        ten.setLocation("here");
        ten.setRetries(2);
        conf.addIncludeRange(ten);

        conf.addExcludeRange(new ExcludeRange("192.168.1.1", "192.168.1.254"));
        conf.addExcludeRange(new ExcludeRange("192.168.2.1", "192.168.2.254"));

        final IncludeUrl includeUrl = new IncludeUrl("file:/opt/opennms/etc/include.txt");
        includeUrl.setLocation("everywhere");
        includeUrl.setForeignSource("blah");
        conf.addIncludeUrl(includeUrl);

        final ExcludeUrl excludeUrl = new ExcludeUrl("file:/opt/opennms/etc/exclude.txt");
        excludeUrl.setLocation("everywhere");
        excludeUrl.setForeignSource("blah");
        conf.addExcludeUrl(excludeUrl);

        final DiscoveryConfiguration exampleConf = new DiscoveryConfiguration();
        exampleConf.setPacketsPerSecond(1d);
        exampleConf.setInitialSleepTime(30000l);
        exampleConf.setRestartSleepTime(86400000l);
        exampleConf.setRetries(1);
        exampleConf.setTimeout(2000l);
        
        final IncludeRange exampleRange = new IncludeRange("192.168.0.1", "192.168.0.254");
        exampleConf.addIncludeRange(exampleRange);
        
        exampleConf.addIncludeUrl(new IncludeUrl("file:/opt/opennms/etc/include.txt"));
        exampleConf.addIncludeUrl(new IncludeUrl("http://example.com/ip-address-list.txt"));

        exampleConf.addExcludeUrl(new ExcludeUrl("file:/opt/opennms/etc/exclude.txt"));
        exampleConf.addExcludeUrl(new ExcludeUrl("http://example-exclude.com/ip-address-list.txt"));



        return Arrays.asList(new Object[][] {
            {
                conf,
                "<discovery-configuration packets-per-second=\"1.0\"\n" +
                        "        initial-sleep-time=\"30000\" restart-sleep-time=\"86400000\"\n" + 
                        "        retries=\"1\" timeout=\"2000\">\n" + 
                        "    <include-range timeout=\"3000\" foreign-source=\"blah\">\n" + 
                        "        <begin>192.168.0.1</begin>\n" + 
                        "        <end>192.168.254.254</end>\n" + 
                        "    </include-range>\n" +
                        "    <include-range location=\"here\" retries=\"2\">\n" + 
                        "        <begin>10.0.0.1</begin>\n" + 
                        "        <end>10.0.0.254</end>\n" + 
                        "    </include-range>\n" +
                        "    <exclude-range>\n" + 
                        "        <begin>192.168.1.1</begin>\n" + 
                        "        <end>192.168.1.254</end>\n" + 
                        "    </exclude-range>\n" +
                        "    <exclude-range>\n" + 
                        "        <begin>192.168.2.1</begin>\n" + 
                        "        <end>192.168.2.254</end>\n" + 
                        "    </exclude-range>\n" +
                        "    <include-url location=\"everywhere\" foreign-source=\"blah\">file:/opt/opennms/etc/include.txt</include-url>\n" +
                        "    <exclude-url location=\"everywhere\" foreign-source=\"blah\">file:/opt/opennms/etc/exclude.txt</exclude-url>\n" +
                        "</discovery-configuration>"
            },
            {
                exampleConf,
                "<discovery-configuration packets-per-second=\"1.0\"\n" +
                "        initial-sleep-time=\"30000\" restart-sleep-time=\"86400000\"\n" + 
                "        retries=\"1\" timeout=\"2000\">\n" + 
                "        <include-range>\n" + 
                "                <begin>192.168.0.1</begin>\n" + 
                "                <end>192.168.0.254</end>\n" + 
                "        </include-range>\n" + 
                "   <include-url>file:/opt/opennms/etc/include.txt</include-url>\n" + 
                "   <include-url>http://example.com/ip-address-list.txt</include-url>\n" +
                "   <exclude-url>file:/opt/opennms/etc/exclude.txt</exclude-url>\n" +
                "   <exclude-url>http://example-exclude.com/ip-address-list.txt</exclude-url>\n" +
                "</discovery-configuration>"
            }
        });
    }

}
