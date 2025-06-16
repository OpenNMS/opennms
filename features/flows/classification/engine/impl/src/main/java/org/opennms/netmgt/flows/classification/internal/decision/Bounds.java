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
package org.opennms.netmgt.flows.classification.internal.decision;

import org.opennms.netmgt.flows.classification.IpAddr;

/**
 * Bundles bounds for the different aspects of flows that are used for classification.
 * <p>
 * Bounds are used during decision tree construction to filter candidate thresholds and classification rules.
 */
public class Bounds {

    public static Bounds ANY = new Bounds(Bound.ANY, Bound.ANY, Bound.ANY, Bound.ANY, Bound.ANY);

    public final Bound<Integer> protocol;
    public final Bound<Integer> srcPort, dstPort;
    public final Bound<IpAddr> srcAddr, dstAddr;

    public Bounds(Bound<Integer> protocol, Bound<Integer> srcPort, Bound<Integer> dstPort, Bound<IpAddr> srcAddr, Bound<IpAddr> dstAddr) {
        this.protocol = protocol;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.srcAddr = srcAddr;
        this.dstAddr = dstAddr;
    }
}
