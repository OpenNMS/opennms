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
package org.opennms.netmgt.flows.classification;

import java.io.InputStream;
import java.util.List;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.flows.classification.exception.CSVImportException;
import org.opennms.netmgt.flows.classification.exception.InvalidRuleException;
import org.opennms.netmgt.flows.classification.persistence.api.Group;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;

public interface ClassificationService {
    List<Rule> findMatchingRules(Criteria criteria);

    int countMatchingRules(Criteria criteria);

    Rule getRule(int ruleId);

    Integer saveRule(Rule rule) throws InvalidRuleException;

    void deleteRules(int groupId);

    void deleteRule(int ruleId);

    void updateRule(Rule rule);

    List<Group> findMatchingGroups(Criteria criteria);

    int countMatchingGroups(Criteria criteria);

    Group getGroup(int groupId);

    Integer saveGroup(Group group);

    void deleteGroup(int groupId);

    void updateGroup(Group group);

    void importRules(int groupId, InputStream inputStream, boolean hasHeader, boolean deleteExistingRules) throws CSVImportException;

    String exportRules(int groupId);

    String classify(ClassificationRequest classificationRequest);

    List<Rule> getInvalidRules();

    void validateRule(Rule validateMe);
}
