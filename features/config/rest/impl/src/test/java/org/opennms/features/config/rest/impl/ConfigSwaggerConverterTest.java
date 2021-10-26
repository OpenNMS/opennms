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

package org.opennms.features.config.rest.impl;


import com.google.common.io.Resources;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.junit.Test;
import org.opennms.features.config.dao.api.ConfigItem;
import org.opennms.features.config.dao.api.util.XsdModelConverter;
import org.skyscreamer.jsonassert.JSONAssert;

import javax.ws.rs.core.MediaType;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
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

        assertThat(api.getPaths().keySet(), contains("/svc", "/svc/{configId}"));
    }

    @Test
    public void canConvertXsd() throws IOException {
        final XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        final URL url = Resources.getResource(XSD_PATH);
        try (InputStream is = url.openStream()) {
            schemaCol.read(new StreamSource(is));
        }

        XsdModelConverter xsdModelConverter = new XsdModelConverter();
        ConfigItem configItem = xsdModelConverter.convert(schemaCol, TOP_ELEMENT);

        ConfigSwaggerConverter configSwaggerConverter = new ConfigSwaggerConverter();
        String openapiStr = configSwaggerConverter.convertToString(configItem, "/VacuumdConfiguration", MediaType.APPLICATION_JSON);

        final String expectedSwaggerJson = Resources.toString(
                Resources.getResource("swagger.generated.json"), StandardCharsets.UTF_8);

        JSONAssert.assertEquals(expectedSwaggerJson, openapiStr, true);
    }
}
