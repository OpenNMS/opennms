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

package org.opennms.features.telemetry.adapters.nxos;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.telemetry.adapters.nxos.proto.TelemetryBis;

public class NxTelmetryClient {

    private static TelemetryBis.Telemetry buildMessage(String nodeId, String subsriptionId) {

        // Set Telemetry fields
        TelemetryBis.TelemetryField field = TelemetryBis.TelemetryField.newBuilder()
                                                .setName("ProtoName")
                                                .setStringValue("ipv4")
                                                .setTimestamp(1508775858).build();
        
        final TelemetryBis.Telemetry telemetrymsg = TelemetryBis.Telemetry.newBuilder()
                                                        .setNodeIdStr(nodeId)
                                                        .addDataGpbkv(field)
                                                        .setSubscriptionIdStr(subsriptionId)
                                                        .setCollectionId(4)
                                                        .setCollectionStartTime(1508775858)
                                                        .setCollectionEndTime(1508775875)
                                                        .setMsgTimestamp(new Date().getTime())
                                                        .build();
        

        return telemetrymsg;

    }
    
    public static void main(String... args) throws IOException {
        // Not sure what could be nodeId and subscription Id. This is just assumption
        TelemetryBis.Telemetry jtiMsg = buildMessage("192.168.2.1", "eth0");
        byte[] jtiMsgBytes = jtiMsg.toByteArray();

        InetAddress address = InetAddressUtils.getLocalHostAddress();
        DatagramPacket packet = new DatagramPacket(jtiMsgBytes, jtiMsgBytes.length, address, 50000);
        DatagramSocket socket = new DatagramSocket();
        socket.send(packet);
    }

}
