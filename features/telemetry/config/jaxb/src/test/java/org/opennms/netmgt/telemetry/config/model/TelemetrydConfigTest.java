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
import org.opennms.netmgt.telemetry.config.model.Adapter;
import org.opennms.netmgt.telemetry.config.model.Filter;
import org.opennms.netmgt.telemetry.config.model.Listener;
import org.opennms.netmgt.telemetry.config.model.Package;
import org.opennms.netmgt.telemetry.config.model.Parameter;
import org.opennms.netmgt.telemetry.config.model.Protocol;
import org.opennms.netmgt.telemetry.config.model.Rrd;
import org.opennms.netmgt.telemetry.config.model.TelemetrydConfiguration;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

public class TelemetrydConfigTest extends XmlTestNoCastor<TelemetrydConfiguration> {
    public TelemetrydConfigTest(TelemetrydConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/telemetryd-config.xsd");
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws ParseException, UnknownHostException {
        TelemetrydConfiguration telemetrydConfig = new TelemetrydConfiguration();

        Protocol jtiProtocol = new Protocol();
        jtiProtocol.setName("JTI");
        jtiProtocol.setDescription("Junos Telemetry Interface (JTI)");
        jtiProtocol.setEnabled(false);
        telemetrydConfig.getProtocols().add(jtiProtocol);

        Listener udpListener = new Listener();
        udpListener.setName("JTI-UDP-50000");
        udpListener.setClassName("org.opennms.netmgt.collection.streaming.udp.UdpListener");
        udpListener.getParameters().add(new Parameter("port", "50000"));
        jtiProtocol.getListeners().add(udpListener);

        Adapter jtiGbpAdapter = new Adapter();
        jtiGbpAdapter.setName("JTI-GPB");
        jtiGbpAdapter.setClassName("org.opennms.netmgt.collection.streaming.jti.JtiGpbAdapter");
        jtiGbpAdapter.getParameters().add(new Parameter("script", "${install.dir}/etc/telemetryd-adapters/junos-telemetry-interface.groovy"));
        jtiProtocol.getAdapters().add(jtiGbpAdapter);

        Package jtiDefaultPkg = new Package();
        jtiDefaultPkg.setName("JTI-Default");
        jtiDefaultPkg.setFilter(new Filter("IPADDR != '0.0.0.0'"));
        jtiProtocol.getPackages().add(jtiDefaultPkg);

        Rrd rrd = new Rrd();
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
                "  <protocol name=\"JTI\" description=\"Junos Telemetry Interface (JTI)\" enabled=\"false\">\n" +
                "    <listener name=\"JTI-UDP-50000\" class-name=\"org.opennms.netmgt.collection.streaming.udp.UdpListener\">\n" +
                "      <parameter key=\"port\" value=\"50000\"/>\n" +
                "    </listener>\n" +
                "\n" +
                "    <adapter name=\"JTI-GPB\" class-name=\"org.opennms.netmgt.collection.streaming.jti.JtiGpbAdapter\">\n" +
                "      <parameter key=\"script\" value=\"${install.dir}/etc/telemetryd-adapters/junos-telemetry-interface.groovy\" />\n" +
                "    </adapter>\n" +
                "\n" +
                "    <package name=\"JTI-Default\">\n" +
                "      <filter>IPADDR != '0.0.0.0'</filter>\n" +
                "      <rrd step=\"300\">\n" +
                "        <rra>RRA:AVERAGE:0.5:1:2016</rra>\n" +
                "        <rra>RRA:AVERAGE:0.5:12:1488</rra>\n" +
                "        <rra>RRA:AVERAGE:0.5:288:366</rra>\n" +
                "        <rra>RRA:MAX:0.5:288:366</rra>\n" +
                "        <rra>RRA:MIN:0.5:288:366</rra>\n" +
                "      </rrd>\n" +
                "    </package>\n" +
                "  </protocol>\n" +
                "</telemetryd-config>"
                } });
    }
}
