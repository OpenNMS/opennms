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

package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats;

public interface Metric {
    void accept(final Visitor visitor);

    interface Visitor {
        void visit(final DuplicatePrefix duplicatePrefix);
        void visit(final DuplicateWithdraws duplicateWithdraws);
        void visit(final AdjRibIn adjRibIn);
        void visit(final AdjRibOut adjRibOut);
        void visit(final ExportRib exportRib);
        void visit(final InvalidUpdateDueToAsConfedLoop invalidUpdateDueToAsConfedLoop);
        void visit(final InvalidUpdateDueToAsPathLoop invalidUpdateDueToAsPathLoop);
        void visit(final InvalidUpdateDueToClusterListLoop invalidUpdateDueToClusterListLoop);
        void visit(final InvalidUpdateDueToOriginatorId invalidUpdateDueToOriginatorId);
        void visit(final PerAfiAdjRibIn perAfiAdjRibIn);
        void visit(final PerAfiLocRib perAfiLocRib);
        void visit(final PrefixTreatAsWithdraw prefixTreatAsWithdraw);
        void visit(final UpdateTreatAsWithdraw updateTreatAsWithdraw);
        void visit(final LocRib locRib);
        void visit(final DuplicateUpdate duplicateUpdate);
        void visit(final Rejected rejected);
    }
}
