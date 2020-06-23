/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification.persistence.api;

import java.util.Comparator;
import java.util.Objects;

/**
 * Compares first the group position and the the rules position.
 */
public class RulePositionComparator implements Comparator<RuleDefinition> {
    @Override
    public int compare(RuleDefinition r1, RuleDefinition r2) {
        Objects.requireNonNull(r1);
        Objects.requireNonNull(r2);

        // Sort by group position (lowest position first)
        int groupPosition1 = r1.getGroupPosition();
        int groupPosition2 = r2.getGroupPosition();
        int result = Integer.compare(groupPosition1, groupPosition2);

        // If group position is identical, sort by rule position (lowest position first)
        if (result == 0) {
            return Integer.compare(r1.getPosition(), r2.getPosition());
        }
        return result;
    }
}
