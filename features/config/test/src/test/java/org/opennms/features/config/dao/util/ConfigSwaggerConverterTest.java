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

package org.opennms.features.config.dao.util;


import com.google.common.io.Resources;
import io.swagger.v3.oas.models.OpenAPI;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.opennms.features.config.dao.api.ConfigItem;
import org.opennms.features.config.dao.impl.util.ConfigSwaggerConverter;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.dao.impl.util.XsdModelConverter;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

public class ConfigSwaggerConverterTest {
    public static final String XSD_PATH = "xsds/fake-vacuumd-configuration.xsd";
    public static final String TOP_ELEMENT = "VacuumdConfiguration";

    @Test
    public void convertSimpleModel() {
        ConfigItem parent = new ConfigItem();
        parent.setName("parent");
        parent.setType(ConfigItem.Type.OBJECT);

        ConfigItem array = new ConfigItem();
        array.setName("array");
        array.setType(ConfigItem.Type.ARRAY);
        parent.getChildren().add(array);

        ConfigItem object = new ConfigItem();
        object.setName("object");
        object.setType(ConfigItem.Type.OBJECT);
        array.getChildren().add(object);

        ConfigItem property = new ConfigItem();
        property.setName("property");
        property.setType(ConfigItem.Type.STRING);
        object.getChildren().add(property);

        ConfigSwaggerConverter configSwaggerConverter = new ConfigSwaggerConverter();
        OpenAPI api = configSwaggerConverter.convert(parent, "/svc");

        MatcherAssert.assertThat(api.getPaths().keySet(), contains("/svc", "/svc/{configId}"));
    }

    @Test
    public void canConvertXsd() throws IOException {
        String xsdStr = Resources.toString(XsdHelper.getSchemaPath(XSD_PATH), StandardCharsets.UTF_8);

        XsdModelConverter xsdModelConverter = new XsdModelConverter(xsdStr);
        ConfigItem configItem = xsdModelConverter.convert(TOP_ELEMENT);

        ConfigSwaggerConverter configSwaggerConverter = new ConfigSwaggerConverter();
        String openapiStr = configSwaggerConverter.convertToString(configItem, "/VacuumdConfiguration",
                ConfigSwaggerConverter.APPLICATION_JSON);

        final String expectedSwaggerJson = Resources.toString(
                Resources.getResource("swagger.generated.json"), StandardCharsets.UTF_8);

        JSONAssert.assertEquals(expectedSwaggerJson, openapiStr, true);
    }
}
