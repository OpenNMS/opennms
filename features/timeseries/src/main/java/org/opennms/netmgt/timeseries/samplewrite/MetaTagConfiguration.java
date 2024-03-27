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
package org.opennms.netmgt.timeseries.samplewrite;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Defines which additional meta tags should be exposed to the timeseries integration plugin.
 */
public class MetaTagConfiguration {

    final static String CONFIG_PREFIX = "org.opennms.timeseries.tin.metatags";
    final static String CONFIG_KEY_FOR_CATEGORIES = CONFIG_PREFIX + ".exposeCategories";
    final static String CONFIG_PREFIX_FOR_TAGS = CONFIG_PREFIX + ".tag.";

    private final boolean categoriesEnabled;

    private final Map<String, String> configuredMetaTags;

    public MetaTagConfiguration(final Map<String, String> properties) {
        this.categoriesEnabled = Optional.ofNullable(properties.get(CONFIG_KEY_FOR_CATEGORIES)).map(Boolean::valueOf).orElse(false);
        this.configuredMetaTags = findConfiguredMetaTags(properties);
    }

    public Map<String, String> getConfiguredMetaTags() {
        return this.configuredMetaTags;
    }

    public boolean isCategoriesEnabled() {
        return this.categoriesEnabled;
    }

    private Map<String, String> findConfiguredMetaTags(final Map<String, String> properties) {
        Map<String, String> filteredMap = new HashMap<>();
        return properties
                .entrySet()
                .stream()
                .filter(e -> e.getKey().startsWith(MetaTagConfiguration.CONFIG_PREFIX_FOR_TAGS))
                .collect(Collectors.toMap((entry) -> entry.getKey().substring(MetaTagConfiguration.CONFIG_PREFIX_FOR_TAGS.length()), Map.Entry::getValue));
    }
}
