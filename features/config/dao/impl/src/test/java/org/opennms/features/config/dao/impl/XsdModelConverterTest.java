/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
package org.opennms.features.config.dao.impl;

import org.junit.Test;
import org.opennms.features.config.dao.api.ConfigSchema;
import org.opennms.features.config.dao.impl.util.ValidateUsingConverter;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;

import javax.xml.bind.JAXBException;
import java.io.IOException;


public class XsdModelConverterTest {
    final String configName = "testConfigName";
    final int majorVersion = 29;

    @Test
    public void testData() throws IOException, JAXBException {
        // register
        ValidateUsingConverter<ProvisiondConfiguration> converter = new ValidateUsingConverter<>(ProvisiondConfiguration.class);
        ConfigSchema<ValidateUsingConverter> configSchema = new ConfigSchema<>(configName, majorVersion,
                0, 0, ValidateUsingConverter.class, converter);
        configSchema.getConverter().getValidationSchema().getConfigItem();

    }
}
