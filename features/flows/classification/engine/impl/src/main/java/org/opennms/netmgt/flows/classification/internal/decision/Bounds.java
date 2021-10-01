/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
