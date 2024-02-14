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
package org.opennms.netmgt.telemetry.protocols.nxos.adapter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis;

/**
 * Utility to send Nxos messages to Telemetry stack
 **/
public class NxosTelemetryClient {

    private static TelemetryBis.Telemetry buildMessage(String ipAddress , int i) throws IOException {

        // Set Telemetry fields
        TelemetryBis.TelemetryField field1 = TelemetryBis.TelemetryField.newBuilder()
                                                .setName("loadavg")
                                                .setUint32Value(i+32)
                                                .setTimestamp(1510584351).build();
        

        final TelemetryBis.Telemetry telemetrymsg = TelemetryBis.Telemetry.newBuilder()
                                                        .setNodeIdStr(ipAddress)
                                                        .addDataGpbkv(field1)
                                                        .setSubscriptionIdStr("18374686715878047745")
                                                        .setCollectionId(10456)
                                                        .setCollectionStartTime(1510584351)
                                                        .setCollectionEndTime(1510584402)
                                                        .setMsgTimestamp(new Date().getTime())
                                                        .build();
        return telemetrymsg;

    }
    
    public static void main(String... args) throws IOException {
        
        List<Integer> numbers = new ArrayList<>();
        for ( int i=0; i < 1000; i++) {
            numbers.add(i);
        }
        numbers.parallelStream().forEach( i -> {
            try {
                sendNxosPacket(i);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    
    public static void sendNxosPacket(int i) throws IOException {
        
        TelemetryBis.Telemetry nxosMsg = buildMessage("192.168.0.106", i);
        byte[] nxosMsgBytes = nxosMsg.toByteArray();

        InetAddress address = InetAddressUtils.getLocalHostAddress();
        DatagramPacket packet = new DatagramPacket(nxosMsgBytes, nxosMsgBytes.length, address, 50001);
        DatagramSocket socket = new DatagramSocket();
        socket.send(packet);
    }

}
