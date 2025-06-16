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
package org.opennms.netmgt.flows.classification.persistence.api;

import java.util.Comparator;
import java.util.Objects;

/**
 * Compares first the group position and the the rules position.
 */
public class RulePositionComparator implements Comparator<RuleDefinition> {

    public static RulePositionComparator INSTANCE = new RulePositionComparator();

    private RulePositionComparator() {}

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
