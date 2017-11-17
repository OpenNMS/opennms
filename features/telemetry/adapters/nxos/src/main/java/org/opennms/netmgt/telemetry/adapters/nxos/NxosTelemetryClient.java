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

package org.opennms.netmgt.telemetry.adapters.nxos;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.adapters.nxos.proto.TelemetryBis;


public class NxosTelemetryClient {

    private static TelemetryBis.Telemetry buildMessage(String ipAddress) throws IOException {

        // Set Telemetry fields
        TelemetryBis.TelemetryField field1 = TelemetryBis.TelemetryField.newBuilder()
                                                .setName("loadavg")
                                                .setUint32Value(23)
                                                .setTimestamp(1510584351).build();
        

        final TelemetryBis.Telemetry telemetrymsg = TelemetryBis.Telemetry.newBuilder()
                                                        .setNodeIdStr(ipAddress)
                                                        .addDataGpbkv(field1)
                                                        .setSubscriptionIdStr("18374686715878047745")
                                                        .setCollectionId(4)
                                                        .setCollectionStartTime(1510584351)
                                                        .setCollectionEndTime(1510584402)
                                                        .setMsgTimestamp(new Date().getTime())
                                                        .build();
        return telemetrymsg;

    }
    
    public static void main(String... args) throws IOException {
        // Making assumption that NodeId is IpAddress
        TelemetryBis.Telemetry nxosMsg = buildMessage("192.168.2.1");
        byte[] nxosMsgBytes = nxosMsg.toByteArray();

        InetAddress address = InetAddressUtils.getLocalHostAddress();
        DatagramPacket packet = new DatagramPacket(nxosMsgBytes, nxosMsgBytes.length, address, 50001);
        DatagramSocket socket = new DatagramSocket();
        socket.send(packet);
    }

}
