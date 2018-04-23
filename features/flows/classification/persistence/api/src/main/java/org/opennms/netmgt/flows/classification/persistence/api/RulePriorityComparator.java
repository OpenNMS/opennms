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

// The more concrete a rule is, the higher the priority should be.
// However, if a name and protocol is defined, but a rule with a concrete port/address (src or dst) this rule wins.
public class RulePriorityComparator implements Comparator<RuleDefinition> {
    @Override
    public int compare(RuleDefinition r1, RuleDefinition r2) {
        Objects.requireNonNull(r1);
        Objects.requireNonNull(r2);

        // Sort by group priority (highest priority first)
        int groupPriority1 = r1.getGroupPriority();
        int groupPriority2 = r2.getGroupPriority();
        int result = -1 * Integer.compare(groupPriority1, groupPriority2);

        // If group priority is identical, sort by rule priority (highest priority first)
        if (result == 0) {
            return -1 * Integer.compare(r1.calculatePriority(), r2.calculatePriority());
        }
        return result;
    }
}
