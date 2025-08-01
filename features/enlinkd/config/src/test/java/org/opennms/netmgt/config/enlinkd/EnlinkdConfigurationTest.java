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
package org.opennms.netmgt.config.enlinkd;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class EnlinkdConfigurationTest extends XmlTestNoCastor<EnlinkdConfiguration> {

    public EnlinkdConfigurationTest(EnlinkdConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/enlinkd-configuration.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getConfig(),
                "<enlinkd-configuration threads=\"3\" \n" +
                "                     executor-queue-size=\"100\"\n" +
                "                     executor-threads=\"5\"\n" +
                "                     discovery-bridge-threads=\"1\"\n" +
                "                     initial_sleep_time=\"60000\"\n" +
                "                     bridge_topology_interval=\"300000\"\n" +
                "                     topology_interval=\"30000\"\n" +
                "                     cdp_rescan_interval=\"86400000\" \n" +
                "                     lldp_rescan_interval=\"86400000\" \n" +
                "                     bridge_rescan_interval=\"86400000\" \n" +
                "                     ospf_rescan_interval=\"86400000\" \n" +
                "                     isis_rescan_interval=\"86400000\" \n" +
                "                     cdp-priority=\"1000\" \n" +
                "                     lldp-priority=\"2000\" \n" +
                "                     bridge-priority=\"10000\" \n" +
                "                     ospf-priority=\"3000\" \n" +
                "                     isis-priority=\"4000\" \n" +
                "                     use-cdp-discovery=\"true\"\n" +
                "                     use-bridge-discovery=\"true\"\n" + 
                "                     use-lldp-discovery=\"true\"\n" + 
                "                     use-ospf-discovery=\"true\"\n" + 
                "                     use-isis-discovery=\"true\"\n" +
                "                     disable-bridge-vlan-discovery=\"false\"\n" +
                "                     max_bft=\"100\"\n" +
                "                     />"
            }
        });
    }

    private static EnlinkdConfiguration getConfig() {
        EnlinkdConfiguration config = new EnlinkdConfiguration();
        config.setExecutorThreads(5);
        config.setExecutorQueueSize(100);
        config.setThreads(3);
        config.setDiscoveryBridgeThreads(1);
        config.setInitialSleepTime(60000L);
        config.setBridgeTopologyInterval(300000L);
        config.setTopologyInterval(30000L);
        config.setCdpRescanInterval(86400000L);
        config.setLldpRescanInterval(86400000L);
        config.setBridgeRescanInterval(86400000L);
        config.setOspfRescanInterval(86400000L);
        config.setIsisRescanInterval(86400000L);
        config.setCdpPriority(1000);
        config.setLldpPriority(2000);
        config.setBridgePriority(10000);
        config.setOspfPriority(3000);
        config.setIsisPriority(4000);
        config.setUseCdpDiscovery(true);
        config.setUseBridgeDiscovery(true);
        config.setUseLldpDiscovery(true);
        config.setUseOspfDiscovery(true);
        config.setUseIsisDiscovery(true);
        config.setDisableBridgeVlanDiscovery(false);
        config.setMaxBft(100);
        return config;
    }
}
