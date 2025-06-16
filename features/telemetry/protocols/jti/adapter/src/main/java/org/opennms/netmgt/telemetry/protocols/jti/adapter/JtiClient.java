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
package org.opennms.netmgt.telemetry.protocols.jti.adapter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.protocols.jti.adapter.proto.TelemetryTop;
import org.opennms.netmgt.telemetry.protocols.jti.adapter.proto.Port;


/**
 * Example code for generating JTI messages.
 *
 * This example requires a node with interface 192.168.2.1 to exist in the database
 * for the messages to be handled:
 *  ./bin/send-event.pl --interface 192.168.2.1 uei.opennms.org/internal/discovery/newSuspect
 *
 * @author jwhite
 */
public class JtiClient {

    private static TelemetryTop.TelemetryStream buildJtiMessage(String ipAddress, String ifName, long ifInOctets, long ifOutOctets) {
        final Port.GPort port =
               Port.GPort.newBuilder()
                .addInterfaceStats(org.opennms.netmgt.telemetry.protocols.jti.adapter.proto.Port.InterfaceInfos.newBuilder()
                        .setIfName(ifName)
                        .setInitTime(1457647123)
                        .setSnmpIfIndex(517)
                        .setParentAeName("ae0")
                        .setIngressStats(Port.InterfaceStats.newBuilder()
                                .setIfOctets(ifInOctets)
                                .setIfPkts(1)
                                .setIf1SecPkts(1)
                                .setIf1SecOctets(1)
                                .setIfUcPkts(1)
                                .setIfMcPkts(1)
                                .setIfBcPkts(1)
                                .build())
                        .setEgressStats(Port.InterfaceStats.newBuilder()
                                .setIfOctets(ifOutOctets)
                                .setIfPkts(1)
                                .setIf1SecPkts(1)
                                .setIf1SecOctets(1)
                                .setIfUcPkts(1)
                                .setIfMcPkts(1)
                                .setIfBcPkts(1)
                                .build())
                        .build())
                .build();

        final TelemetryTop.JuniperNetworksSensors juniperNetworksSensors = TelemetryTop.JuniperNetworksSensors.newBuilder()
                .setExtension(Port.jnprInterfaceExt, port)
                .build();

        final TelemetryTop.EnterpriseSensors sensors = TelemetryTop.EnterpriseSensors.newBuilder()
                .setExtension(TelemetryTop.juniperNetworks, juniperNetworksSensors)
                .build();

        final TelemetryTop.TelemetryStream jtiMsg = TelemetryTop.TelemetryStream.newBuilder()
                .setSystemId(ipAddress)
                .setComponentId(0)
                .setSensorName("intf-stats")
                .setSequenceNumber(49103)
                .setTimestamp(new Date().getTime())
                .setEnterprise(sensors)
                .build();

        return jtiMsg;
    }

    public static void main(String... args) throws IOException {
        TelemetryTop.TelemetryStream jtiMsg = buildJtiMessage("192.168.2.1", "eth0", 100, 100);
        byte[] jtiMsgBytes = jtiMsg.toByteArray();

        InetAddress address = InetAddressUtils.getLocalHostAddress();
        DatagramPacket packet = new DatagramPacket(jtiMsgBytes, jtiMsgBytes.length, address, 50000);
        DatagramSocket socket = new DatagramSocket();
        socket.send(packet);
    }
}
