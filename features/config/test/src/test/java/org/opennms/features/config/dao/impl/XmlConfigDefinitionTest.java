/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
 ******************************************************************************/
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
