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
package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp;

import java.util.Map;
import java.util.TreeMap;

public class PeerInfo {
    private final static int BGP_CAP_ADD_PATH_RECEIVE = 1;
    private final static int BGP_CAP_ADD_PATH_SEND = 2;
    private final static int BGP_CAP_ADD_PATH_SEND_RECEIVE = 3;

    private class SendReceiveCodeForSentAndReceivedOpenMessage {
        public int sendReceiveCodeForSentOpenMessage;
        public int sendReceiveCodeForReceivedOpenMessage;
    }

    final Map<Integer, Map<Integer, SendReceiveCodeForSentAndReceivedOpenMessage>> addPathCapabilityMap = new TreeMap<>();

    public void addPathCapability(final int afi, final int safi, final int sendReceive, final boolean sentOpen) {
        if (sentOpen) {
            addPathCapabilityMap.computeIfAbsent(
                    afi,
                    k -> new TreeMap<>()
            ).computeIfAbsent(
                    safi,
                    k -> new SendReceiveCodeForSentAndReceivedOpenMessage()
            ).sendReceiveCodeForSentOpenMessage = sendReceive;
        } else {
            addPathCapabilityMap.computeIfAbsent(
                    afi,
                    k -> new TreeMap<>()
            ).computeIfAbsent(
                    safi,
                    k -> new SendReceiveCodeForSentAndReceivedOpenMessage()
            ).sendReceiveCodeForReceivedOpenMessage = sendReceive;
        }
    }

    public boolean isAddPathEnabled(final int afi, final int safi) {
        if (addPathCapabilityMap.containsKey(afi)) {
            if (addPathCapabilityMap.get(afi).containsKey(safi)) {
                final SendReceiveCodeForSentAndReceivedOpenMessage s = addPathCapabilityMap.get(afi).get(safi);
                return (s.sendReceiveCodeForSentOpenMessage == BGP_CAP_ADD_PATH_RECEIVE || s.sendReceiveCodeForSentOpenMessage == BGP_CAP_ADD_PATH_SEND_RECEIVE) && (s.sendReceiveCodeForReceivedOpenMessage == BGP_CAP_ADD_PATH_SEND || s.sendReceiveCodeForReceivedOpenMessage == BGP_CAP_ADD_PATH_SEND_RECEIVE);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
