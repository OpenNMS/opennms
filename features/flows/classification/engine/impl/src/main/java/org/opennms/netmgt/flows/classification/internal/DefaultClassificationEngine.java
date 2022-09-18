/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.ReloadingClassificationEngine;
import org.opennms.netmgt.flows.classification.dto.RuleDTO;
import org.opennms.netmgt.flows.classification.internal.decision.PreprocessedRule;
import org.opennms.netmgt.flows.classification.internal.decision.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * A classification engine that uses a decision tree to select applicable classification rules.
 * <p>
 * The implementation is thread-safe.
 */
public class DefaultClassificationEngine implements ClassificationEngine, ReloadingClassificationEngine {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultClassificationEngine.class);

    private final AtomicReference<TreeAndInvalidRules> treeAndInvalidRules = new AtomicReference<>(new TreeAndInvalidRules(Tree.EMPTY, Collections.emptyList()));

    public DefaultClassificationEngine() {}

    @Override
    public void load(final Collection<RuleDTO> rules) throws InterruptedException {
        var start = System.currentTimeMillis();
        var invalid = new ArrayList<RuleDTO>(); // TODO fooker: remove this.

        // Load all rules and validate them
        final List<PreprocessedRule> preprocessedRules = Lists.newArrayList();
        rules.forEach(rule -> {
            try {
                final var preprocessedRule = PreprocessedRule.of(rule);
                preprocessedRules.add(preprocessedRule);
            } catch (Exception ex) {
                LOG.error("Rule {} is not valid. Ignoring rule.", rule, ex);
                invalid.add(rule);
            }
        });

        var tree = Tree.of(preprocessedRules);

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
        private final List<RuleDTO> invalidRules;
        public TreeAndInvalidRules(Tree tree, List<RuleDTO> invalidRules) {
            this.tree = tree;
            this.invalidRules = invalidRules;
        }
    }
}
