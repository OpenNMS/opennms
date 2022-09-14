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

package org.opennms.netmgt.flows.classification.service.internal;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.FilterWatcher;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.flows.classification.dto.RuleDTO;
import org.opennms.netmgt.flows.classification.persistence.api.Protocol;
import org.opennms.netmgt.flows.classification.persistence.api.Protocols;
import org.opennms.netmgt.flows.classification.persistence.api.RuleDefinition;
import org.opennms.netmgt.flows.classification.service.internal.csv.CsvServiceImpl;
import org.opennms.netmgt.flows.classification.service.internal.validation.GroupValidator;
import org.opennms.netmgt.flows.classification.service.internal.validation.RuleValidator;
import org.opennms.netmgt.flows.classification.persistence.api.ClassificationGroupDao;
import org.opennms.netmgt.flows.classification.persistence.api.ClassificationRuleDao;
import org.opennms.netmgt.flows.classification.persistence.api.Group;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.service.ClassificationService;
import org.opennms.netmgt.flows.classification.service.csv.CsvImportResult;
import org.opennms.netmgt.flows.classification.service.csv.CsvService;
import org.opennms.netmgt.flows.classification.service.error.ErrorContext;
import org.opennms.netmgt.flows.classification.service.error.Errors;
import org.opennms.netmgt.flows.classification.service.exception.CSVImportException;
import org.opennms.netmgt.flows.classification.service.exception.ClassificationException;
import org.opennms.netmgt.flows.classification.service.exception.InvalidRuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

public class DefaultClassificationService implements ClassificationService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultClassificationService.class);

    private final ClassificationRuleDao classificationRuleDao;

    private final ClassificationGroupDao classificationGroupDao;

    private final RuleValidator ruleValidator;

    private final CsvService csvService;

    private final GroupValidator groupValidator;

    private final SessionUtils sessionUtils;

    private final FilterDao filterDao;

    private final FilterWatcher.Session filterWatcher;

    private final Set<Consumer<List<RuleDTO>>> listeners = Sets.newConcurrentHashSet();
    private final TwinPublisher.Session<List<RuleDTO>> publisher;

    public DefaultClassificationService(final ClassificationRuleDao classificationRuleDao,
                                        final ClassificationGroupDao classificationGroupDao,
                                        final FilterDao filterDao,
                                        final FilterWatcher filterWatcher,
                                        final SessionUtils sessionUtils,
                                        final TwinPublisher twinPublisher) throws IOException {
        this.classificationRuleDao = Objects.requireNonNull(classificationRuleDao);
        this.classificationGroupDao = Objects.requireNonNull(classificationGroupDao);
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
        this.filterDao = Objects.requireNonNull(filterDao);
        this.ruleValidator = new RuleValidator(filterDao);
        this.groupValidator = new GroupValidator(classificationRuleDao);
        this.csvService = new CsvServiceImpl(ruleValidator);

        this.publisher = twinPublisher.register(RuleDTO.TWIN_KEY, RuleDTO.TWIN_TYPE, null);

        this.filterWatcher = filterWatcher.watch(Set.of(), this::filtersChanged);

        this.reload();
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
        return runInTransactionAndThenReload(() -> {

            ruleValidator.validate(rule);

            final Group group = classificationGroupDao.get(rule.getGroup().getId());
            if(group == null) {
                throw new NoSuchElementException(String.format("Unknown group with id=%s", rule.getGroup().getId()));
            }
            assertRuleIsNotInReadOnlyGroup(group);
            groupValidator.validate(group, rule);

            // persist
            group.addRule(rule);
            final Integer ruleId = classificationRuleDao.save(rule);
            updateRulePositions(PositionUtil.sortRulePositions(rule));

            return ruleId;
        });
    }

    @Override
    public void importRules(int groupId, InputStream inputStream, boolean hasHeader, boolean deleteExistingRules) throws CSVImportException {
        runInTransactionAndThenReload(() -> {

            // Get and check group
            Group group = classificationGroupDao.get(groupId);
            if(group == null) {
                throw new ClassificationException(ErrorContext.Name, Errors.GROUP_NOT_FOUND, groupId);
            }
            if(group.isReadOnly()) {
                throw new ClassificationException(ErrorContext.Name, Errors.GROUP_READ_ONLY, groupId);
            }

            // Parse and validate the rules
            final CsvImportResult result = csvService.parseCSV(group, inputStream, hasHeader);
            if (!result.isSuccess()) {
                throw new CSVImportException(result);
            }

            // Remove existing rules and afterwards add new rules
            if (deleteExistingRules) {
                for (Rule eachRule : group.getRules()) {
                    classificationRuleDao.delete(eachRule);
                }
                group.getRules().clear();
            }

            // Add new rules
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

            updateRulePositions(PositionUtil.sortRulePositions(group.getRules()));
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
        runInTransactionAndThenReload(() -> {
            Group group = classificationGroupDao.get(groupId);
            if (group == null) throw new NoSuchElementException();
            if(group.isReadOnly()) {
                throw new ClassificationException(ErrorContext.Entity, Errors.GROUP_READ_ONLY);
            }
            final Criteria criteria = criteriaBuilder.toCriteria();
            classificationRuleDao.findMatching(criteria).forEach(r -> classificationRuleDao.delete(r));
            return null;
        });
    }

    @Override
    public void deleteRule(int ruleId) {
        runInTransaction(() -> {
            final Rule rule = classificationRuleDao.get(ruleId);
            if (rule == null) throw new NoSuchElementException();
            assertRuleIsNotInReadOnlyGroup(rule);
            return runInTransactionAndThenReload(() -> {
                // Remove from group, as it would be saved later otherwise
                final Group group = rule.getGroup();
                group.removeRule(rule);
                classificationRuleDao.delete(rule);
                updateRulePositions(PositionUtil.sortRulePositions(group.getRules()));
                return null;
            });
        });
    }

    @Override
    public void updateRule(final Rule rule) {
        if (rule.getId() == null) throw new NoSuchElementException();
        assertRuleIsNotInReadOnlyGroup(rule);

        // Persist
        runInTransactionAndThenReload(() -> {
            ruleValidator.validate(rule);
            groupValidator.validate(rule.getGroup(), rule);
            classificationRuleDao.saveOrUpdate(rule);
            // reload to get updated group since we might just have switched groups
            Rule reloaded = classificationRuleDao.get(rule.getId());
            updateRulePositions(PositionUtil.sortRulePositions(reloaded));
            return null;
        });
    }

    private void assertRuleIsNotInReadOnlyGroup(Rule rule) {
        assertRuleIsNotInReadOnlyGroup(rule.getGroup());
    }

    private void assertRuleIsNotInReadOnlyGroup(Group group) {
        if(group.isReadOnly()) {
            throw new ClassificationException(ErrorContext.Entity, Errors.GROUP_READ_ONLY);
        }
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
    public Integer saveGroup(Group group) {
        checkForDuplicateName(group);
        return runInTransactionAndThenReload(() -> {
            Integer groupId = classificationGroupDao.save(group);
            updateGroupPositions(PositionUtil.sortGroupPositions(group, classificationGroupDao.findAll()));
            return groupId;
        });
    }

    @Override
    public void deleteGroup(int groupId) {
        runInTransactionAndThenReload(() -> {
            final Group group = classificationGroupDao.get(groupId);
            if (group == null) {
                throw new NoSuchElementException();
            } else if(group.isReadOnly()) {
                throw new ClassificationException(ErrorContext.Entity, Errors.GROUP_READ_ONLY);
            }
            classificationGroupDao.delete(group);
            updateGroupPositions(PositionUtil.sortGroupPositions(classificationGroupDao.findAll()));
            return null;
        });
    }

    @Override
    public void updateGroup(Group group) {
        if (group.getId() == null) throw new NoSuchElementException();
        checkForDuplicateName(group);
        runInTransactionAndThenReload(() -> {
            classificationGroupDao.saveOrUpdate(group);
            updateGroupPositions(PositionUtil.sortGroupPositions(group, classificationGroupDao.findAll()));
            updateRulePositions(PositionUtil.sortRulePositions(group.getRules()));
            return null;
        });
    }

    @Override
    public List<Rule> getInvalidRules() {
        return this.classificationRuleDao.findAll()
                                         .stream()
                                         .filter(rule -> {
                                             try {
                                                 this.ruleValidator.validate(rule);
                                                 return false;
                                             } catch (final InvalidRuleException e) {
                                                 return true;
                                             }
                                         }).collect(Collectors.toList());
    }

    @Override
    public void validateRule(final Rule validateMe) {
        Objects.requireNonNull(validateMe);
        ruleValidator.validate(validateMe);
    }

    private <T> T runInTransaction(Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        return sessionUtils.withTransaction(supplier);
    }

    private <T> T runInTransactionAndThenReload(Supplier<T> supplier) {
        T res = runInTransaction(supplier);
        this.reload();
        return res;
    }

    private void updateRulePositions(final List<Rule> rules) {

        // Update position field
        for (int i=0; i<rules.size(); i++) {
            Rule rule = rules.get(i);
            rule.setPosition(i);
            classificationRuleDao.saveOrUpdate(rule);
        }

    }

    private void updateGroupPositions(final List<Group> groups) {

        // Update position field
        for (int i=0; i<groups.size(); i++) {
            Group group = groups.get(i);
            group.setPosition(i);
            classificationGroupDao.saveOrUpdate(group);
        }

    }

    private void checkForDuplicateName (Group group) {
        CriteriaBuilder builder = new CriteriaBuilder(Group.class)
                .eq("name", group.getName());
        if(group.getId() !=null) {
            builder.ne("id", group.getId());
        }
        if(countMatchingGroups(builder.toCriteria()) > 0) {
            throw new ClassificationException(ErrorContext.Entity, Errors.GROUP_NAME_NOT_UNIQUE, group.getName());
        }
    }

    @Override
    public void reload() {
        // TODO fooker: Oh, and implement this

        final var rules = this.classificationRuleDao.findAllEnabledRules();

        // Update the filter watcher for all rules with exporter filters
        this.filterWatcher.setFilters(rules.stream()
                                              .map(Rule::getExporterFilter)
                                              .filter(Predicate.not(Strings::isNullOrEmpty))
                                              .collect(Collectors.toSet()));
    }

    private void filtersChanged(final FilterWatcher.FilterResults results) {
        // TODO fooker: Sort the list and make the position inherent?

        final var result = this.classificationRuleDao.findAllEnabledRules().stream()
                .sorted(Comparator.comparingInt(Rule::getGroupPosition)
                                .thenComparingInt(Rule::getPosition))
                .flatMap(rule -> {
                    if (rule.isOmnidirectional()) {
                        return Stream.of(resolveRule(rule, results), resolveRule(rule.reversedRule(), results));
                    } else {
                        return Stream.of(resolveRule(rule, results));
                    }
                })
                .collect(Collectors.toList());

        this.listeners.forEach(listener -> listener.accept(result));

        try {
            this.publisher.publish(result);
        } catch (final IOException e) {
            LOG.error("Failed to publish classification rules", e);
        }
    }

    private RuleDTO resolveRule(final RuleDefinition rule,
                                final FilterWatcher.FilterResults results) {
        // TODO fooker: error handling of protocols

        final List<String> exporters = Strings.isNullOrEmpty(rule.getExporterFilter())
                                       ? List.of()
                                       : results.getRuleNodeIpServiceMap().getOrDefault(rule.getExporterFilter(), Collections.emptyMap())
                                                .values().stream()
                                                .flatMap(node -> node.keySet().stream())
                                                .map(InetAddressUtils::str)
                                                .collect(Collectors.toList());

        return RuleDTO.builder()
                .withName(rule.getName())
                .withProtocols(Strings.isNullOrEmpty(rule.getProtocol())
                               ? List.of()
                               : Arrays.stream(rule.getProtocol().split(","))
                                       .map(Protocols::getProtocol)
                                       .map(Protocol::getDecimal)
                                       .collect(Collectors.toList()))
                .withSrcPort(rule.getSrcPort())
                .withSrcAddress(rule.getSrcAddress())
                .withDstPort(rule.getDstPort())
                .withDstAddress(rule.getDstAddress())
                .withPosition(rule.getGroupPosition() << 16 | rule.getPosition())
                .withExporters(exporters)
                .build();
    }

    @Override
    public Closeable listen(final Consumer<List<RuleDTO>> listener) {
        this.listeners.add(listener);
        return () -> this.listeners.remove(listener);
    }
}
