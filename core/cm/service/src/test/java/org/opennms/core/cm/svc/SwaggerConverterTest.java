/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021-2021 The OpenNMS Group, Inc.
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

package org.opennms.core.cm.svc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.stream.StreamSource;

import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

import io.swagger.v3.oas.models.OpenAPI;

public class SwaggerConverterTest {

    @Test
    public void canConvertXsd() throws IOException {
        final XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        final URL url = Resources.getResource("vacuumd-configuration-test.xsd");
        try (InputStream is = url.openStream()) {
            schemaCol.read(new StreamSource(is));
        }

        SwaggerConverter converter = new SwaggerConverter();
        OpenAPI actualApi = converter.convert(schemaCol);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        final String actualSwaggerJson = objectMapper.writeValueAsString(actualApi);

        final String expectedSwaggerJson = Resources.toString(
                Resources.getResource("swagger.generated.json"), StandardCharsets.UTF_8);

        System.out.println("Actual JSON: " + actualSwaggerJson);
        JSONAssert.assertEquals(expectedSwaggerJson, actualSwaggerJson, true);
    }
}
