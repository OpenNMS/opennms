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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opennms.netmgt.timeseries.samplewrite.MetaTagConfiguration.CONFIG_KEY_FOR_CATEGORIES;
import static org.opennms.netmgt.timeseries.samplewrite.MetaTagConfiguration.CONFIG_PREFIX_FOR_TAGS;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class MetaTagConfigurationTest {

    private MetaTagConfiguration metaTagConfiguration;

    @Before
    public void setUp() {
        Map<String, String> properties = new HashMap<>();
        properties.put(CONFIG_PREFIX_FOR_TAGS + "label", "node:label");
        properties.put(CONFIG_PREFIX_FOR_TAGS + "admin", "asset:admin");
        properties.put(CONFIG_KEY_FOR_CATEGORIES, "true");
        this.metaTagConfiguration = new MetaTagConfiguration(properties);
    }

    @Test
    public void shouldReturnEnabledMetaTags() {
        assertEquals("node:label", metaTagConfiguration.getConfiguredMetaTags().get("label"));
        assertEquals("asset:admin", metaTagConfiguration.getConfiguredMetaTags().get("admin"));
    }

    @Test
    public void shouldReturnEnabledCategory() {
        assertTrue(metaTagConfiguration.isCategoriesEnabled());
    }
}
