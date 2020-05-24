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
package org.opennms.netmgt.timeseries.integration;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Defines which additional meta tags should be exposed to the timeseries integration plugin. */
public class MetaTagConfiguration {

    final static String PREFIX = "org.opennms.timeseries.tin.metatags";

    /** properties defined in opennms.properties */
    public enum PropertyKey {
        assets,
        tags,
        categories
    }

    public enum MetaTagKey {
        nodeLabel,
        location,
        sysObjectID,
        foreignSource,
        foreignId,
        nodeCriteria,
        ipAddress, // for response time resources
        service, // for response time resources
        ifDescr, // for interface resources
        ifAlias, // for interface resources
        resourceLabel
    }

    // TODO: Patrick: do we want to merge MetaTagKey & AssetTagKey?
    public enum AssetTagKey {
        admin, additionalHardware, assetNumber,
    }

    private final Set<MetaTagKey> enabledMetaTags;
    private final Set<String> enabledCategories;
    private final Set<AssetTagKey> enabledAssets;

    public MetaTagConfiguration(final Map<String, String> properties) {

        final Set<String> configuredAssets = getAsList(getProperty(properties, PropertyKey.assets));
        enabledAssets = Arrays
                .stream(AssetTagKey.values())
                .filter(key -> configuredAssets.contains(key.name()))
                .collect(Collectors.toSet());

        enabledCategories = getAsList(getProperty(properties, PropertyKey.categories));

        final Set<String> configuredTags = getAsList(getProperty(properties, PropertyKey.tags));
        enabledMetaTags = Arrays
                .stream(MetaTagKey.values())
                .filter(key -> configuredTags.contains(key.name()))
                .collect(Collectors.toSet());

        // TODO: Patrick Meta-Data DSL expressions to build tags
    }

    public boolean isEnabled(final AssetTagKey key) {
        return enabledAssets.contains(key);
    }

    public boolean isEnabled(final MetaTagKey key) {
        return enabledMetaTags.contains(key);
    }

    public boolean isCategoryEnabled(final String category) {
        return this.enabledCategories.contains(category);
    }

    private Set<String> getAsList(final String value) {
        if(value == null || value.trim().isEmpty()) {
            return Collections.emptySet();
        }
        return Arrays
                .stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
        .collect(Collectors.toSet());
    }

    private String getProperty(final Map<String, String> properties, final PropertyKey property) {
        return properties.get(PREFIX + "." + property.name());
    }
}
