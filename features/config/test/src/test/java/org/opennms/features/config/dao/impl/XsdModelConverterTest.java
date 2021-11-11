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

import com.google.common.io.Resources;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.config.dao.api.ConfigItem;
import org.opennms.features.config.dao.impl.util.SchemaUtil;
import org.opennms.features.config.dao.impl.util.XmlConverter;
import org.opennms.features.config.dao.impl.util.XsdModelConverter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class XsdModelConverterTest {

    @Test
    public void testData() throws IOException {
        // register
        XmlConverter converter = new XmlConverter("provisiond-configuration.xsd", "provisiond-configuration");

        XsdModelConverter xsdConverter = new XsdModelConverter();
        String xsdStr = Resources.toString(SchemaUtil.getSchemaPath("provisiond-configuration.xsd"), StandardCharsets.UTF_8);
        XmlSchemaCollection collection = xsdConverter.convertToSchemaCollection(xsdStr);
        ConfigItem item = xsdConverter.convert(collection, "provisiond-configuration");

        Assert.assertEquals("Should have enough children items", 7, item.getChildren().size());
        Assert.assertEquals("Should have correct schema ref.",
                "{http://xmlns.opennms.org/xsd/config/provisiond-configuration}provisiond-configuration", item.getSchemaRef());
        Assert.assertNotNull("Should have documentation", item.getDocumentation());
    }
}
