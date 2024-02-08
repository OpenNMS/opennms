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
