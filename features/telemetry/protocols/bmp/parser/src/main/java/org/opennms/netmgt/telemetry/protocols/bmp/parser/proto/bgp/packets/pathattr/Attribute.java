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
