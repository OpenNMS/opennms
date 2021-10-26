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
 *******************************************************************************/

package org.opennms.features.config.dao.impl;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.opennms.features.config.dao.api.ConfigSchema;
import org.opennms.features.config.dao.impl.util.XmlConverter;


public class XsdModelConverterTest {
    final String configName = "testConfigName";

    @Test
    public void testData() throws IOException, JAXBException {
        // register
        XmlConverter converter = new XmlConverter("provisiond-configuration.xsd", "provisiond-configuration");
        ConfigSchema<XmlConverter> configSchema = new ConfigSchema<>(configName, XmlConverter.class, converter);
        configSchema.getConverter().getValidationSchema().getConfigItem();

    }
}
