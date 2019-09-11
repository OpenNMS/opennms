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

package org.opennms.netmgt.flows.classification.internal;

import java.util.Comparator;
import java.util.List;

import org.opennms.netmgt.flows.classification.persistence.api.Group;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;

class PositionUtil {

    /** Sorts the rules by its position. The given rule gets the lower position as another rule with the same position
     * => the given rule will be evaluated before another rule with the same position. */
    static List<Rule> sortRulePositions(Rule rule) {
        // Load all rules of group and sort by position (lowest first) in that group
        final List<Rule> rules = rule.getGroup().getRules();
        rules.sort(Comparator
                .comparing(Rule::getPosition)
                .thenComparing(r -> !r.getId().equals(rule.getId()))); // "our rule" will have the lower position move other rules away
        return rules;
    }

    /** Sorts the rules by its position.*/
    static List<Rule> sortRulePositions(List<Rule> rules ) {
        // Sort by position in that group
        rules.sort(Comparator
            .comparing(Rule::getPosition));
        return rules;
    }

    /** Sorts the groups by its position. The given group gets the lower position as another group with the same position
     * => the given group will be evaluated before another group with the same position. */
    static List<Group> sortGroupPositions(Group group, List<Group> groups) {
        groups.sort(Comparator
                .comparing(Group::isReadOnly)
                .thenComparing(Group::getPosition)
                .thenComparing(g -> !g.getId().equals(group.getId()))); // "our group" will have the lower position move other groups away
        return groups;
    }

    /** Sorts the rules by its position.*/
    static List<Group> sortGroupPositions(List<Group> groups ) {
        groups.sort(Comparator
                .comparing(Group::isReadOnly)
                .thenComparing(Group::getPosition));
        return groups;
    }
}
