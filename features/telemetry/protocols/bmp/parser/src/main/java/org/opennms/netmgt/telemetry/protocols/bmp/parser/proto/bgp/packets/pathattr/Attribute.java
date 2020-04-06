/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr;

public interface Attribute {
    void accept(final Visitor visitor);

    interface Visitor {
        void visit(final Aggregator aggregator);
        void visit(final AsPath asPath);
        void visit(final AtomicAggregate atomicAggregate);
        void visit(final LocalPref localPref);
        void visit(final MultiExistDisc multiExistDisc);
        void visit(final NextHop nextHop);
        void visit(final Origin origin);
        void visit(final Community community);
        void visit(final OriginatorId originatorId);
        void visit(final ClusterList clusterList);
        void visit(final ExtendedCommunities extendedCommunities);
        void visit(final ExtendedV6Communities extendedV6Communities);
        void visit(final Connector connector);
        void visit(final AsPathLimit asPathLimit);
        void visit(final LargeCommunities largeCommunity);
        void visit(final AttrSet attrSet);
        void visit(final Unknown unknown);
        void visit(final MultiprotocolReachableNlri multiprotocolReachableNlri);
        void visit(final MultiprotocolUnreachableNlri multiprotocolUnreachableNlri);
    }
}
