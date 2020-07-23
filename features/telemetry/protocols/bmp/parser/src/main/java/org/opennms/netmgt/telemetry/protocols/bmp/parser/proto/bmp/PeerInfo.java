/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
