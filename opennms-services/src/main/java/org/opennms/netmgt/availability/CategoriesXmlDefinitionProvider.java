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
import java.util.List;

import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.api.CatFactory;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.config.categories.CategoryGroup;

/**
 * Reads category definitions from categories.xml. Each category's effective
 * rule is its group's common rule ANDed with its own rule, the same
 * composition the rest of the system applies to these categories.
 */
public class CategoriesXmlDefinitionProvider implements CategoryDefinitionProvider {

    @Override
    public List<CategoryDefinition> getDefinitions() {
        final CatFactory factory;
        try {
            CategoryFactory.init();
            factory = CategoryFactory.getInstance();
        } catch (final IOException e) {
            throw new IllegalStateException("Cannot load categories.xml", e);
        }

        final List<CategoryDefinition> definitions = new ArrayList<>();
        factory.getReadLock().lock();
        try {
            for (final CategoryGroup group : factory.getConfig().getCategoryGroups()) {
                final String commonRule = group.getCommon() == null ? null : group.getCommon().getRule();
                for (final Category category : group.getCategories()) {
                    definitions.add(new CategoryDefinition(category.getLabel(), effectiveRule(commonRule, category.getRule()), category.getServices(),
                            category.getNormalThreshold(), category.getWarningThreshold()));
                }
            }
        } finally {
            factory.getReadLock().unlock();
        }
        return definitions;
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
