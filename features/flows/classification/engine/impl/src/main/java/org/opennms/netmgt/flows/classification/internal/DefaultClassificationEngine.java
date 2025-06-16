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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.ClassificationRuleProvider;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.internal.decision.PreprocessedRule;
import org.opennms.netmgt.flows.classification.internal.decision.Tree;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * A classification engine that uses a decision tree to select applicable classification rules.
 * <p>
 * The implementation is thread-safe.
 */
public class DefaultClassificationEngine implements ClassificationEngine {

    private List<ClassificationRulesReloadedListener> classificationRulesReloadedListeners = new ArrayList<>();

    private static Logger LOG = LoggerFactory.getLogger(DefaultClassificationEngine.class);

    private final AtomicReference<TreeAndInvalidRules> treeAndInvalidRules = new AtomicReference<>(new TreeAndInvalidRules(Tree.EMPTY, Collections.emptyList()));

    private final ClassificationRuleProvider ruleProvider;
    private final FilterService filterService;

    public DefaultClassificationEngine(final ClassificationRuleProvider ruleProvider, final FilterService filterService) throws InterruptedException {
        this(ruleProvider, filterService, true);
    }

    public DefaultClassificationEngine(final ClassificationRuleProvider ruleProvider, final FilterService filterService, final boolean initialize) throws InterruptedException {
        this.ruleProvider = Objects.requireNonNull(ruleProvider);
        this.filterService = Objects.requireNonNull(filterService);
        if (initialize) {
            this.reload();
        }
    }

    @Override
    public void reload() throws InterruptedException {
        var start = System.currentTimeMillis();
        var invalid = new ArrayList<Rule>();

        // Load all rules and validate them
        final List<PreprocessedRule> preprocessedRules = Lists.newArrayList();
        final var rules = ruleProvider.getRules();
        rules.forEach(rule -> {
            try {
                final var preprocessedRule = PreprocessedRule.of(rule);
                preprocessedRules.add(preprocessedRule);
                if (rule.canBeReversed()) {
                    preprocessedRules.add(preprocessedRule.reverse());
                }
            } catch (Exception ex) {
                LoggerFactory.getLogger(getClass()).error("Rule {} is not valid. Ignoring rule.", rule, ex);
                invalid.add(rule);
            }
        });

        var tree = Tree.of(preprocessedRules, filterService);

        var elapsed = System.currentTimeMillis() - start;
        if (LOG.isInfoEnabled()) {
            var sb = new StringBuilder();
            sb
                    .append("calculated flow classification decision tree\n")
                    .append("time (ms): " + elapsed).append('\n')
                    .append("rules    : " + rules.size() + " (including reversed rules: " + preprocessedRules.size() + ")").append('\n')
                    .append("leaves   : " + tree.info.leaves).append('\n')
                    .append("nodes    : " + tree.info.nodes).append('\n')
                    .append("choices  : " + tree.info.choices).append(" (nodes with rules that ignore the aspect of the node's threshold)\n")
                    .append("minDepth : " + tree.info.minDepth).append('\n')
                    .append("maxDepth : " + tree.info.maxDepth).append('\n')
                    .append("avgDepth : " + (double) tree.info.sumDepth / tree.info.leaves).append('\n')
                    .append("minComp  : " + tree.info.minComp).append('\n')
                    .append("maxComp  : " + tree.info.maxComp).append('\n')
                    .append("avgComp  : " + (double) tree.info.sumComp / tree.info.leaves).append('\n')
                    .append("minLeafSize : " + tree.info.minLeafSize).append('\n')
                    .append("maxLeafSize : " + tree.info.maxLeafSize).append('\n')
                    .append("avgLeafSize : " + (double) tree.info.sumLeafSize / tree.info.leaves).append('\n');
            LOG.info(sb.toString());
        }

        treeAndInvalidRules.set(new TreeAndInvalidRules(tree, invalid));

        fireClassificationReloadedListeners(Collections.unmodifiableList(rules));
    }

    private void fireClassificationReloadedListeners(final List<Rule> rules) {
        for(final ClassificationRulesReloadedListener classificationRulesReloadedListener : this.classificationRulesReloadedListeners) {
            classificationRulesReloadedListener.classificationRulesReloaded(rules);
        }
    }

    @Override
    public List<Rule> getInvalidRules() {
        return Collections.unmodifiableList(treeAndInvalidRules.get().invalidRules);
    }

    public Tree getTree() {
        return treeAndInvalidRules.get().tree;
    }

    @Override
    public String classify(ClassificationRequest classificationRequest) {
        return treeAndInvalidRules.get().tree.classify(classificationRequest);
    }

    private static class TreeAndInvalidRules {
        private final Tree tree;
        private final List<Rule> invalidRules;
        public TreeAndInvalidRules(Tree tree, List<Rule> invalidRules) {
            this.tree = tree;
            this.invalidRules = invalidRules;
        }
    }

    public void addClassificationRulesReloadedListener(final ClassificationRulesReloadedListener classificationRulesReloadedListener) {
        this.classificationRulesReloadedListeners.add(classificationRulesReloadedListener);
    }

    public void removeClassificationRulesReloadedListener(final ClassificationRulesReloadedListener classificationRulesReloadedListener) {
        this.classificationRulesReloadedListeners.remove(classificationRulesReloadedListener);
    }
}
