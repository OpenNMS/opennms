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

import com.atlassian.oai.validator.report.EmptyValidationReport;
import com.atlassian.oai.validator.report.ValidationReport;
import org.junit.Test;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.springframework.util.Assert;

public class XmlConfigDefinitionTest {
    ConfigDefinition def = XsdHelper.buildConfigDefinition("provisiond", "provisiond-configuration.xsd",
            "provisiond-configuration", ConfigurationManagerService.BASE_PATH);

    @Test
    public void testPassValidation() {
        ValidationReport report = def.validate("{\"importThreads\": 11}");
        Assert.isInstanceOf(EmptyValidationReport.class, report, "It should be empty report!");
    }

    @Test
    public void testFailValidation() {
        ValidationReport report = def.validate("{\"importThreads\": -1}");
        Assert.isTrue(report.hasErrors(), "It should detect -1.");

        report = def.validate("{\"importThreads\": \"test\"}");
        Assert.isTrue(report.hasErrors(), "It should detect invalid datatype.");

        report = def.validate("{\"test\": 11}");
        Assert.isTrue(report.hasErrors(), "It should detect invalid attribute.");
    }
}
