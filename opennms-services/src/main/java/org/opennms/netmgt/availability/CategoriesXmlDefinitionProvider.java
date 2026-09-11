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
package org.opennms.netmgt.availability;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.api.CatFactory;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.config.categories.CategoryGroup;
import org.opennms.netmgt.config.categories.Catinfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads category definitions from categories.xml. Each category's effective
 * rule is its group's common rule ANDed with its own rule, the same
 * composition the rest of the system applies to these categories.
 *
 * Labels are unique across groups: the first definition of a label wins and
 * later ones are logged and skipped, as are categories without any rule, so a
 * bad entry in the file disables that category rather than the daemon.
 */
public class CategoriesXmlDefinitionProvider implements CategoryDefinitionProvider {
    private static final Logger LOG = LoggerFactory.getLogger(CategoriesXmlDefinitionProvider.class);

    @Override
    public List<CategoryDefinition> getDefinitions() {
        final CatFactory factory;
        try {
            CategoryFactory.init();
            factory = CategoryFactory.getInstance();
        } catch (final IOException e) {
            throw new IllegalStateException("Cannot load categories.xml", e);
        }

        factory.getReadLock().lock();
        try {
            return toDefinitions(factory.getConfig());
        } finally {
            factory.getReadLock().unlock();
        }
    }

    /** Package-private for tests: build definitions from an already parsed categories.xml. */
    static List<CategoryDefinition> toDefinitions(final Catinfo config) {
        final Map<String, CategoryDefinition> byLabel = new LinkedHashMap<>();
        for (final CategoryGroup group : config.getCategoryGroups()) {
            final String commonRule = group.getCommon() == null ? null : group.getCommon().getRule();
            for (final Category category : group.getCategories()) {
                final String label = category.getLabel();
                if (label == null || label.trim().isEmpty()) {
                    LOG.warn("Skipping a category without a label in group '{}'", group.getName());
                    continue;
                }
                if (byLabel.containsKey(label)) {
                    LOG.warn("Skipping duplicate category '{}' in group '{}'; the first definition is used", label, group.getName());
                    continue;
                }
                final String rule;
                try {
                    rule = effectiveRule(commonRule, category.getRule());
                } catch (final IllegalArgumentException e) {
                    LOG.warn("Skipping category '{}' in group '{}': {}", label, group.getName(), e.getMessage());
                    continue;
                }
                byLabel.put(label, new CategoryDefinition(label, rule, category.getServices(), category.getNormalThreshold(), category.getWarningThreshold()));
            }
        }
        return new ArrayList<>(byLabel.values());
    }

    static String effectiveRule(final String commonRule, final String rule) {
        final boolean hasCommon = commonRule != null && !commonRule.trim().isEmpty();
        final boolean hasRule = rule != null && !rule.trim().isEmpty();
        if (hasCommon && hasRule) {
            return "(" + commonRule + ") & (" + rule + ")";
        }
        if (hasRule) {
            return rule;
        }
        if (hasCommon) {
            return commonRule;
        }
        throw new IllegalArgumentException("Category has neither a rule nor a common rule");
    }
}
