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

    void deleteGroup(int groupId);

    void updateGroup(Group group);

    void importRules(InputStream inputStream, boolean hasHeader, boolean deleteExistingRules) throws CSVImportException;

    String exportRules(int groupId);

    String classify(ClassificationRequest classificationRequest);
}
