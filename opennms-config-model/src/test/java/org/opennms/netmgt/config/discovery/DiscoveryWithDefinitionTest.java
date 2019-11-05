/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
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
