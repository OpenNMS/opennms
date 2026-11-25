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
package org.opennms.netmgt.icmp.jna;

import org.opennms.jicmp.jna.NativeDatagramSocket;

/**
 * The socket implementation a pinger opens its ICMP sockets with.
 * A backend never falls back to the other one.
 */
public enum SocketBackend {
    FFM,
    @Deprecated(since = "37", forRemoval = true)
    JNA;

    /**
     * FFM calls {@link NativeDatagramSocket#createFfm}, JNA calls {@link NativeDatagramSocket#createJna}.
     */
    public NativeDatagramSocket open(final int family, final int protocol, final int listenPort) throws Exception {
        switch (this) {
        case FFM:
            return NativeDatagramSocket.createFfm(family, protocol, listenPort);
        case JNA:
            return NativeDatagramSocket.createJna(family, protocol, listenPort);
        default:
            throw new IllegalStateException("Unknown socket backend " + this);
        }
    }
}
