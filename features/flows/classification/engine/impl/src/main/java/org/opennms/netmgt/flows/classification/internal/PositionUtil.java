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
