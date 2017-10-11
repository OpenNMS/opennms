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

package org.opennms.netmgt.telemetry.adapters.jti;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.adapters.jti.proto.Port;
import org.opennms.netmgt.telemetry.adapters.jti.proto.TelemetryTop;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;

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
        final Port.GPort port = Port.GPort.newBuilder()
                .addInterfaceStats(Port.InterfaceInfos.newBuilder()
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
