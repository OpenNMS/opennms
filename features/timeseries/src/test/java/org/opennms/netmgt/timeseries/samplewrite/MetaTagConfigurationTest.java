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
