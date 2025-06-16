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
package org.opennms.features.config.dao.impl;

import org.junit.Test;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.features.config.service.api.ConfigurationManagerService;

public class XmlConfigDefinitionTest {
    ConfigDefinition def = XsdHelper.buildConfigDefinition("provisiond", "provisiond-configuration.xsd",
            "provisiond-configuration", ConfigurationManagerService.BASE_PATH, false);

    @Test
    public void testPassValidation() {
        def.validate("{\"importThreads\": 11}");
    }

    @Test(expected = ValidationException.class)
    public void testInvalidValue() {
        // It should detect -1.
        def.validate("{\"importThreads\": -1}");
    }

    @Test(expected = ValidationException.class)
    public void testWrongType() {
        // It should detect invalid datatype.
        def.validate("{\"importThreads\": \"test\"}");
    }

    @Test(expected = ValidationException.class)
    public void testInvalidAttribute() {
        // It should detect invalid attribute.
        def.validate("{\"test\": 11}");
    }
}
