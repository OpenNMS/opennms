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
package org.opennms.web.rest.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.opennms.web.rest.v2.ResourceTypesConfigRestServiceTest.strategy;
import static org.opennms.web.rest.v2.ResourceTypesConfigRestServiceTest.typeDto;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.opennms.web.rest.v2.model.ResourceTypeDto;
import org.opennms.web.rest.v2.model.ResourceTypesConfigDto;

public class ResourceTypesConfigValidatorTest {

    @Test
    public void acceptsAValidConfiguration() {
        final ResourceTypeDto type = typeDto("pgTableSpace");
        type.setStorageStrategy(strategy("org.opennms.netmgt.dao.support.SiblingColumnStorageStrategy",
                "sibling-column-name", "spcname", "replace", "s/\\s//", "replace", "s/:\\\\//"));
        assertNull(ResourceTypesConfigValidator.validateConfig(config("postgresql-resource", type)));
        assertNull(ResourceTypesConfigValidator.validateConfig(config("empty.config_1")));
    }

    @Test
    public void checksTheConfigurationId() {
        assertNotNull(ResourceTypesConfigValidator.validateConfigId(null));
        assertNotNull(ResourceTypesConfigValidator.validateConfigId(" "));
        assertNotNull(ResourceTypesConfigValidator.validateConfigId("../etc"));
        assertNotNull(ResourceTypesConfigValidator.validateConfigId("with blank"));
        assertNotNull(ResourceTypesConfigValidator.validateConfigId("-leading"));
        assertNull(ResourceTypesConfigValidator.validateConfigId("wsman-dell-idrac"));
    }

    @Test
    public void requiresNameLabelAndStrategies() {
        final ResourceTypeDto noName = typeDto("x");
        noName.setName(" ");
        assertMessage("the name is required", noName);

        final ResourceTypeDto slash = typeDto("a/b");
        assertMessage("must not contain", slash);

        final ResourceTypeDto noLabel = typeDto("x");
        noLabel.setLabel("");
        assertMessage("label of 'x' is required", noLabel);

        final ResourceTypeDto noPersistence = typeDto("x");
        noPersistence.setPersistenceSelectorStrategy(null);
        assertMessage("persistence selector strategy of 'x': the class is required", noPersistence);

        final ResourceTypeDto badClass = typeDto("x");
        badClass.setStorageStrategy(strategy("not a class"));
        assertMessage("not a valid Java class name", badClass);
    }

    @Test
    public void requiresParameterKeysAndValues() {
        final ResourceTypeDto noKey = typeDto("x");
        noKey.setStorageStrategy(strategy("a.B", " ", "v"));
        assertMessage("key of parameter 1 is required", noKey);

        // the configuration manager drops empty values, which then fail its schema check
        final ResourceTypeDto noValue = typeDto("x");
        noValue.setStorageStrategy(strategy("a.B", "k", ""));
        assertMessage("value of parameter 'k' is required", noValue);
    }

    @Test
    public void rejectsDuplicateNamesWithinAConfiguration() {
        assertEquals("Resource type 'dup' is defined more than once.",
                ResourceTypesConfigValidator.validateConfig(config("c", typeDto("dup"), typeDto("dup"))));
    }

    private static void assertMessage(final String expected, final ResourceTypeDto type) {
        final String message = ResourceTypesConfigValidator.validateConfig(config("c", type));
        assertNotNull(message);
        assertTrue(message, message.contains(expected));
    }

    private static ResourceTypesConfigDto config(final String id, final ResourceTypeDto... types) {
        final ResourceTypesConfigDto dto = new ResourceTypesConfigDto();
        dto.setId(id);
        dto.setResourceTypes(new ArrayList<>(List.of(types)));
        return dto;
    }
}
