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
package org.opennms.smoketest.stacks;

import static org.opennms.smoketest.stacks.InternetProtocol.UDP;
import static org.opennms.smoketest.stacks.InternetProtocol.TCP;

import java.util.Objects;

/**
 * Network protocols used by our services.
 *
 * This includes both ports for management and communication from devices.
 *
 */
public enum NetworkProtocol {
    SSH(TCP),
    HTTP(TCP),

    // Java Debug Wire Protocol
    JDWP(TCP),

    SYSLOG(UDP),
    SNMP(UDP),
    JTI(UDP),
    NXOS(UDP),
    FLOWS(UDP),
    TFTP(UDP),
    BMP(TCP),
    IPFIX_TCP(TCP),
    GRPC(TCP),
    GRAFANA(TCP);

    private final InternetProtocol ipProtocol;

    NetworkProtocol(InternetProtocol ipProtocol) {
        this.ipProtocol = Objects.requireNonNull(ipProtocol);
    }

    public InternetProtocol getIpProtocol() {
        return ipProtocol;
    }
}
