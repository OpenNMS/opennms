/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 * 
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * 
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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

        return Arrays.asList(new Object[][] {
            {
                conf,
                "<discovery-configuration packets-per-second=\"1\"\n" + 
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
                        "</discovery-configuration>"
            },
            {
                exampleConf,
                "<discovery-configuration packets-per-second=\"1\"\n" + 
                "        initial-sleep-time=\"30000\" restart-sleep-time=\"86400000\"\n" + 
                "        retries=\"1\" timeout=\"2000\">\n" + 
                "        <include-range>\n" + 
                "                <begin>192.168.0.1</begin>\n" + 
                "                <end>192.168.0.254</end>\n" + 
                "        </include-range>\n" + 
                "   <include-url>file:/opt/opennms/etc/include.txt</include-url>\n" + 
                "   <include-url>http://example.com/ip-address-list.txt</include-url>\n" + 
                "</discovery-configuration>"
            }
        });
    }

}
