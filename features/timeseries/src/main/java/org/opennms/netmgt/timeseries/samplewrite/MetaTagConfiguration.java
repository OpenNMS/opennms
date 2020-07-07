/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
