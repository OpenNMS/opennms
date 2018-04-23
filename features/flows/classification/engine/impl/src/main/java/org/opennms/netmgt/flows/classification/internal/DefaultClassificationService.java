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

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.ClassificationService;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.csv.CsvImportResult;
import org.opennms.netmgt.flows.classification.csv.CsvService;
import org.opennms.netmgt.flows.classification.exception.CSVImportException;
import org.opennms.netmgt.flows.classification.exception.ClassificationException;
import org.opennms.netmgt.flows.classification.exception.InvalidRuleException;
import org.opennms.netmgt.flows.classification.internal.csv.CsvServiceImpl;
import org.opennms.netmgt.flows.classification.internal.validation.GroupValidator;
import org.opennms.netmgt.flows.classification.internal.validation.RuleValidator;
import org.opennms.netmgt.flows.classification.persistence.api.ClassificationGroupDao;
import org.opennms.netmgt.flows.classification.persistence.api.ClassificationRuleDao;
import org.opennms.netmgt.flows.classification.persistence.api.Group;
import org.opennms.netmgt.flows.classification.persistence.api.Groups;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.persistence.api.RulePriorityComparator;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

public class DefaultClassificationService implements ClassificationService {

    private final ClassificationRuleDao classificationRuleDao;

    private final ClassificationGroupDao classificationGroupDao;

    private final ClassificationEngine classificationEngine;

    private final RuleValidator ruleValidator;

    private final CsvService csvService;

    private final GroupValidator groupValidator;

    private final TransactionOperations transactionTemplate;

    public DefaultClassificationService(ClassificationRuleDao classificationRuleDao,
                                        ClassificationGroupDao classificationGroupDao,
                                        ClassificationEngine classificationEngine,
                                        FilterService filterService,
                                        TransactionOperations transactionOperations) {
        this.classificationRuleDao = Objects.requireNonNull(classificationRuleDao);
        this.classificationGroupDao = Objects.requireNonNull(classificationGroupDao);
        this.classificationEngine = Objects.requireNonNull(classificationEngine);
        this.transactionTemplate = Objects.requireNonNull(transactionOperations);
        this.ruleValidator = new RuleValidator(filterService);
        this.groupValidator = new GroupValidator(classificationRuleDao);
        this.csvService = new CsvServiceImpl(ruleValidator);
    }

    @Override
    public List<Rule> findMatchingRules(Criteria criteria) {
        return classificationRuleDao.findMatching(criteria);
    }

    @Override
    public int countMatchingRules(Criteria criteria) {
        return classificationRuleDao.countMatching(criteria);
    }

    @Override
    public Rule getRule(int ruleId) {
        Rule rule = classificationRuleDao.get(ruleId);
        if (rule == null) throw new NoSuchElementException();
        return rule;
    }

    @Override
    public Integer saveRule(Rule rule) throws InvalidRuleException {
        return runInTransaction((status) -> {
            // All rules are automatically added to the user defined group
            final Group group = classificationGroupDao.findByName(Groups.USER_DEFINED);

            ruleValidator.validate(rule);
            groupValidator.validate(group, rule);

            // persist
            group.addRule(rule);
            final Integer ruleId = classificationRuleDao.save(rule);
            updateRulePositionsAndReloadEngine(group);

            return ruleId;
        });
    }

    @Override
    public void importRules(InputStream inputStream, boolean hasHeader, boolean deleteExistingRules) throws CSVImportException {
        runInTransaction(status -> {
            // Parse and validate the rules
            final CsvImportResult result = csvService.parseCSV(inputStream, hasHeader);
            if (!result.isSuccess()) {
                throw new CSVImportException(result);
            }


            final Group group = classificationGroupDao.findByName(Groups.USER_DEFINED); // Automatically add all rules to the USER_DEFINED space

            // Remove existing rules and afterwards add new rules
            if (deleteExistingRules) {
                for (Rule eachRule : group.getRules()) {
                    eachRule.setGroup(null);
                }
                group.getRules().clear();
            }
            final List<Rule> rules = result.getRules();
            for (int i=0; i<rules.size(); i++) {
                final Rule rule = rules.get(i);
                try {
                    groupValidator.validate(group, rule);
                    group.addRule(rule);
                } catch (ClassificationException ex) {
                    result.markError(i, ex.getError());
                }
            }

            // before continuing, verify everything is okay, otherwise bail
            if (!result.isSuccess()) {
                throw new CSVImportException(result);
            }

            // Reload engine
            updateRulePositionsAndReloadEngine(group);
            classificationGroupDao.saveOrUpdate(group);
            return null;
        });
    }

    @Override
    public String exportRules(int groupId) {
        final Group group = classificationGroupDao.get(groupId);
        if (group == null) throw new NoSuchElementException();
        final String csvContent = csvService.createCSV(group.getRules());
        return csvContent;
    }

    @Override
    public void deleteRules(int groupId) {
        CriteriaBuilder criteriaBuilder = new CriteriaBuilder(Rule.class).alias("group", "group");
        criteriaBuilder.eq("group.id", groupId);
        runInTransaction(status -> {
            final Criteria criteria = criteriaBuilder.toCriteria();
            classificationRuleDao.findMatching(criteria).forEach(r -> classificationRuleDao.delete(r));
            classificationEngine.reload();
            return null;
        });
    }

    @Override
    public void deleteRule(int ruleId) {
        runInTransaction(status -> {
            final Rule rule = classificationRuleDao.get(ruleId);
            if (rule == null) throw new NoSuchElementException();
            return runInTransaction(transactionStatus -> {
                // Remove from group, as it would be saved later otherwise
                final Group group = rule.getGroup();
                group.removeRule(rule);
                classificationRuleDao.delete(rule);
                updateRulePositionsAndReloadEngine(group);
                return null;
            });
        });
    }

    @Override
    public void updateRule(Rule rule) {
        if (rule.getId() == null) throw new NoSuchElementException();

        // Persist
        runInTransaction(status -> {
            ruleValidator.validate(rule);
            groupValidator.validate(rule.getGroup(), rule);
            classificationRuleDao.saveOrUpdate(rule);
            updateRulePositionsAndReloadEngine(rule.getGroup());
            return null;
        });
    }

    @Override
    public String classify(ClassificationRequest classificationRequest) {
        final String classification = classificationEngine.classify(classificationRequest);
        return classification;
    }

    @Override
    public List<Group> findMatchingGroups(Criteria criteria) {
        return classificationGroupDao.findMatching(criteria);
    }

    @Override
    public int countMatchingGroups(Criteria criteria) {
        return classificationGroupDao.countMatching(criteria);
    }

    @Override
    public Group getGroup(int groupId) {
        final Group group = classificationGroupDao.get(groupId);
        if (group == null) throw new NoSuchElementException();
        return group;
    }

    @Override
    public void deleteGroup(int groupId) {
        runInTransaction(status -> {
            final Group group = classificationGroupDao.get(groupId);
            if (group == null) throw new NoSuchElementException();
            classificationGroupDao.delete(group);
            classificationEngine.reload();
            return null;
        });
    }

    @Override
    public void updateGroup(Group group) {
        if (group.getId() == null) throw new NoSuchElementException();

        runInTransaction(status -> {
            classificationGroupDao.saveOrUpdate(group);
            updateRulePositionsAndReloadEngine(group);
            return null;
        });
    }

    private <T> T runInTransaction(TransactionCallback<T> callback) {
        return transactionTemplate.execute(callback);
    }

    private void updateRulePositionsAndReloadEngine(Group group) {
        // Load all rules of group and sort by priority (highest first) in that group
        final List<Rule> rules = group.getRules();
        Collections.sort(rules, new RulePriorityComparator());

        // Update priority field
        for (int i=0; i<rules.size(); i++) {
            Rule rule = rules.get(i);
            rule.setPosition(i);
            classificationRuleDao.saveOrUpdate(rule);
        }

        // Reload engine
        classificationEngine.reload();
    }
}
