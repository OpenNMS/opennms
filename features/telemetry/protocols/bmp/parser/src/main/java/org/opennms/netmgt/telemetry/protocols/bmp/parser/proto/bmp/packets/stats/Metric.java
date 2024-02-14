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
package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats;

public interface Metric {
    void accept(final Visitor visitor);

    interface Visitor {
        void visit(final DuplicatePrefix duplicatePrefix);
        void visit(final DuplicateWithdraw duplicateWithdraw);
        void visit(final AdjRibIn adjRibIn);
        void visit(final AdjRibOut adjRibOut);
        void visit(final ExportRib exportRib);
        void visit(final InvalidUpdateDueToAsConfedLoop invalidUpdateDueToAsConfedLoop);
        void visit(final InvalidUpdateDueToAsPathLoop invalidUpdateDueToAsPathLoop);
        void visit(final InvalidUpdateDueToClusterListLoop invalidUpdateDueToClusterListLoop);
        void visit(final InvalidUpdateDueToOriginatorId invalidUpdateDueToOriginatorId);
        void visit(final PerAfiAdjRibIn perAfiAdjRibIn);
        void visit(final PerAfiLocalRib perAfiLocalRib);
        void visit(final PrefixTreatAsWithdraw prefixTreatAsWithdraw);
        void visit(final UpdateTreatAsWithdraw updateTreatAsWithdraw);
        void visit(final LocalRib localRib);
        void visit(final DuplicateUpdate duplicateUpdate);
        void visit(final Rejected rejected);
        void visit(final PerAfiAdjRibOut perAfiAdjRibOut);
        void visit(final PerAfiExportRib perAfiExportRib);
        void visit(final Unknown unknown);
    }
}
