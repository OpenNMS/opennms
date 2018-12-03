/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.telemetry.config.model;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.XmlTestNoCastor;

import java.util.Arrays;
import java.util.Collection;

public class TelemetrydConfigTest extends XmlTestNoCastor<TelemetrydConfig> {
    public TelemetrydConfigTest(TelemetrydConfig sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/telemetryd-config.xsd");
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        TelemetrydConfig telemetrydConfig = new TelemetrydConfig();

        ListenerConfig udpListener = new ListenerConfig();
        udpListener.setName("JTI-UDP-50000");
        udpListener.setClassName("org.opennms.netmgt.collection.streaming.udp.UdpListener");
        udpListener.setEnabled(true);
        udpListener.getParameters().add(new Parameter("port", "50000"));
        telemetrydConfig.getListeners().add(udpListener);

        ParserConfig jtiParser = new ParserConfig();
        jtiParser.setName("JTI");
        jtiParser.setClassName("org.opennms.netmgt.collection.streaming.jti.JtiParser");
        udpListener.getParsers().add(jtiParser);

        QueueConfig jtiQueue = new QueueConfig();
        jtiQueue.setName("jti");
        telemetrydConfig.getQueues().add(jtiQueue);
        jtiParser.setQueue(jtiQueue);

        AdapterConfig jtiGbpAdapter = new AdapterConfig();
        jtiGbpAdapter.setName("JTI-GPB");
        jtiGbpAdapter.setClassName("org.opennms.netmgt.collection.streaming.jti.JtiGpbAdapter");
        jtiGbpAdapter.getParameters().add(new Parameter("script", "${install.dir}/etc/telemetryd-adapters/junos-telemetry-interface.groovy"));
        jtiGbpAdapter.setEnabled(true);
        jtiQueue.getAdapters().add(jtiGbpAdapter);

        PackageConfig jtiDefaultPkg = new PackageConfig();
        jtiDefaultPkg.setName("JTI-Default");
        jtiDefaultPkg.setFilter(new PackageConfig.Filter("IPADDR != '0.0.0.0'"));
        jtiGbpAdapter.getPackages().add(jtiDefaultPkg);

        PackageConfig.Rrd rrd = new PackageConfig.Rrd();
        rrd.setStep(300);
        rrd.getRras().add("RRA:AVERAGE:0.5:1:2016");
        rrd.getRras().add("RRA:AVERAGE:0.5:12:1488");
        rrd.getRras().add("RRA:AVERAGE:0.5:288:366");
        rrd.getRras().add("RRA:MAX:0.5:288:366");
        rrd.getRras().add("RRA:MIN:0.5:288:366");
        jtiDefaultPkg.setRrd(rrd);

        return Arrays.asList(new Object[][] { {
                telemetrydConfig,
                "<telemetryd-config>\n" +
                "  <listener name=\"JTI-UDP-50000\" class-name=\"org.opennms.netmgt.collection.streaming.udp.UdpListener\" enabled=\"true\">\n" +
                "    <parameter key=\"port\" value=\"50000\"/>\n" +
                "    <parser name=\"JTI\" class-name=\"org.opennms.netmgt.collection.streaming.jti.JtiParser\" queue=\"jti\" />\n" +
                "  </listener>\n" +
                "  \n" +
                "  <queue name=\"jti\">\n" +
                "    <adapter name=\"JTI-GPB\" class-name=\"org.opennms.netmgt.collection.streaming.jti.JtiGpbAdapter\" enabled=\"true\">\n" +
                "      <parameter key=\"script\" value=\"${install.dir}/etc/telemetryd-adapters/junos-telemetry-interface.groovy\" />\n" +
                "      <package name=\"JTI-Default\">\n" +
                "        <filter>IPADDR != '0.0.0.0'</filter>\n" +
                "        <rrd step=\"300\">\n" +
                "          <rra>RRA:AVERAGE:0.5:1:2016</rra>\n" +
                "          <rra>RRA:AVERAGE:0.5:12:1488</rra>\n" +
                "          <rra>RRA:AVERAGE:0.5:288:366</rra>\n" +
                "          <rra>RRA:MAX:0.5:288:366</rra>\n" +
                "          <rra>RRA:MIN:0.5:288:366</rra>\n" +
                "        </rrd>\n" +
                "      </package>\n" +
                "    </adapter>\n" +
                "  </queue>\n" +
                "</telemetryd-config>"
                } });
    }
}
