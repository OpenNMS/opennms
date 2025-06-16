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
package org.opennms.netmgt.flows.classification.internal.validation;

import java.util.Objects;

import org.opennms.netmgt.flows.classification.error.ErrorContext;
import org.opennms.netmgt.flows.classification.error.Errors;
import org.opennms.netmgt.flows.classification.exception.ClassificationException;
import org.opennms.netmgt.flows.classification.persistence.api.ClassificationRuleDao;
import org.opennms.netmgt.flows.classification.persistence.api.Group;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;

public class GroupValidator {

    private final ClassificationRuleDao ruleDao;

    public GroupValidator(final ClassificationRuleDao ruleDao) {
        this.ruleDao = Objects.requireNonNull(ruleDao);
    }

    // verify if adding the given rule to the given group would not result in any conflict(s)
    public void validate(Group group, Rule potentialNewRule) {
        if (potentialNewRule != null) {
            Objects.requireNonNull(group);
            final Rule existingRule = ruleDao.findByDefinition(potentialNewRule, group);
            if (existingRule != null) {
                throw new ClassificationException(ErrorContext.Name, Errors.GROUP_DUPLICATE_RULE);
            }
        }
    }
}
