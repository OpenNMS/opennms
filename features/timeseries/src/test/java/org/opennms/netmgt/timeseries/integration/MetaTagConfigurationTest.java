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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.opennms.netmgt.timeseries.integration.MetaTagConfiguration.AssetTagKey;
import static org.opennms.netmgt.timeseries.integration.MetaTagConfiguration.MetaTagKey;
import static org.opennms.netmgt.timeseries.integration.MetaTagConfiguration.PREFIX;
import static org.opennms.netmgt.timeseries.integration.MetaTagConfiguration.PropertyKey;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class MetaTagConfigurationTest {

    private MetaTagConfiguration metaTagConfiguration;

    @Before
    public void setUp() {
        Map<String, String> properties = new HashMap<>();
        properties.put(PREFIX + "." + PropertyKey.assets, String.join(",", AssetTagKey.admin.name(), AssetTagKey.assetNumber.name()));
        properties.put(PREFIX + "." + PropertyKey.categories, "myCategory");
        properties.put(PREFIX + "." + PropertyKey.tags, String.join(",", MetaTagKey.nodeLabel.name(), MetaTagKey.foreignId.name()));
        this.metaTagConfiguration = new MetaTagConfiguration(properties);
    }

    @Test
    public void shouldReturnEnabledAsset() {
        assertTrue(metaTagConfiguration.isEnabled(AssetTagKey.admin));
        assertTrue(metaTagConfiguration.isEnabled(AssetTagKey.assetNumber));
        assertFalse(metaTagConfiguration.isEnabled(AssetTagKey.additionalHardware));
        assertFalse(metaTagConfiguration.isEnabled((AssetTagKey) null));
    }

    @Test
    public void shouldReturnEnabledCategory() {
        assertTrue(metaTagConfiguration.isCategoryEnabled("myCategory"));
        assertFalse(metaTagConfiguration.isCategoryEnabled("notConfiguredCategory"));
        assertFalse(metaTagConfiguration.isCategoryEnabled(""));
        assertFalse(metaTagConfiguration.isCategoryEnabled(null));
    }

    @Test
    public void shouldReturnEnabledMetaTag() {
        assertTrue(metaTagConfiguration.isEnabled(MetaTagKey.nodeLabel));
        assertTrue(metaTagConfiguration.isEnabled(MetaTagKey.foreignId));
        assertFalse(metaTagConfiguration.isEnabled(MetaTagKey.sysObjectID));
        assertFalse(metaTagConfiguration.isEnabled((MetaTagKey) null));
    }
}
