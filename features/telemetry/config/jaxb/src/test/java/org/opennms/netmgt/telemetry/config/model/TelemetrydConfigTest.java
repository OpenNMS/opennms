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

        QueueConfig openConfigQueue = new QueueConfig();
        openConfigQueue.setName("openconfig");
        telemetrydConfig.getQueues().add(openConfigQueue);

        ConnectorConfig openConfigConnector = new ConnectorConfig();
        openConfigConnector.setQueue(openConfigQueue);
        openConfigConnector.setName("OpenConfig-Connector");
        openConfigConnector.setClassName("org.opennms.netmgt.telemetry.connectors.OpenConfigConnector");
        openConfigConnector.setServiceName("OpenConfig");
        openConfigConnector.setEnabled(true);
        telemetrydConfig.getConnectors().add(openConfigConnector);

        PackageConfig openConfigPkg = new PackageConfig();
        openConfigPkg.setFilter(new PackageConfig.Filter("catIncX"));
        openConfigPkg.setName("OpenConfig-Default");
        openConfigPkg.getParameters().add(new Parameter("port", "8000"));
        openConfigConnector.getPackages().add(openConfigPkg);

        return Arrays.asList(new Object[][] { {
                telemetrydConfig,
                "<telemetryd-config>\n" +
                "  <listener name=\"JTI-UDP-50000\" class-name=\"org.opennms.netmgt.collection.streaming.udp.UdpListener\" enabled=\"true\">\n" +
                "    <parameter key=\"port\" value=\"50000\"/>\n" +
                "    <parser name=\"JTI\" class-name=\"org.opennms.netmgt.collection.streaming.jti.JtiParser\" queue=\"jti\" />\n" +
                "  </listener>\n" +
                "  <connector name=\"OpenConfig-Connector\" class-name=\"org.opennms.netmgt.telemetry.connectors.OpenConfigConnector\" service-name=\"OpenConfig\" queue=\"openconfig\" enabled=\"true\">\n" +
                "    <package name=\"OpenConfig-Default\">\n" +
                "      <filter>catIncX</filter>\n" +
                "      <parameter key=\"port\" value=\"8000\"/>\n" +
                "   </package>\n" +
                "  </connector>\n" +
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
                "  <queue name=\"openconfig\"/>\n" +
                "</telemetryd-config>"
                } });
    }
}
