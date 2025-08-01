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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.config.dao.api.ConfigItem;
import org.opennms.features.config.dao.impl.util.ConfigSwaggerConverter;
import org.opennms.features.config.dao.impl.util.OpenAPIBuilder;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.skyscreamer.jsonassert.JSONAssert;

import java.math.BigDecimal;

public class OpenAPIBuilderTest {
    String configName = "configName";
    String elementName = "element";

    @Test
    public void testBuildOpenAPI() {
        OpenAPI openapi = OpenAPIBuilder.createBuilder(configName, elementName, ConfigurationManagerService.BASE_PATH)
                .addStringAttribute("att1", null, null, "^\\d{3}-\\d{3}$", "val1", true, "att1 doc")
                .addBooleanAttribute("bool1", false, false, "bool att")
                .addArray("arr1", ConfigItem.Type.INTEGER, null, 100L, 20L, null, 10L, null, null, true, "test array")
                .build(false);
        Assert.assertArrayEquals("Should generate 2 paths.",
                new String[]{"/rest/cm/configName", "/rest/cm/configName/{configId}"}, openapi.getPaths().keySet().toArray());
        Assert.assertEquals("Should generate operations for /rest/cm/configName.",
                1, openapi.getPaths().get("/rest/cm/configName").readOperations().size());
        Assert.assertEquals("Should generate operations for /rest/cm/configName/{configId}.",
                4, openapi.getPaths().get("/rest/cm/configName/{configId}").readOperations().size());
        Assert.assertEquals("Should generate schemas.", 1, openapi.getComponents().getSchemas().size());
        ObjectSchema schema = (ObjectSchema) openapi.getComponents().getSchemas().get(elementName);
        Assert.assertEquals("Should generate properties.", 3, schema.getProperties().size());
        Assert.assertEquals("Should generate att1.", "^\\d{3}-\\d{3}$", schema.getProperties().get("att1").getPattern());
        Assert.assertEquals("Should generate bool1.", false, schema.getProperties().get("bool1").getDefault());
        ArraySchema array = (ArraySchema) schema.getProperties().get("arr1");
        Assert.assertEquals("Should generate arr1 max item.", (Integer) 100, array.getMaxItems());
        Assert.assertEquals("Should have correct arr1 data type.", "integer", array.getItems().getType());
        Assert.assertEquals("Should have correct arr1 multipleOf.", new BigDecimal(10), array.getItems().getMultipleOf());
    }

    @Test
    public void testBuildNestedSingleConfigOpenAPI() {
        OpenAPI openapi = OpenAPIBuilder.createBuilder(configName, elementName, ConfigurationManagerService.BASE_PATH)
                .addDateTimeAttribute("att1", null, true, "date time doc")
                .addArray("arr1", OpenAPIBuilder.createBuilder()
                        .addStringAttribute("name", 3L, 10L, null, null, true, "val1")
                        .addNumberAttribute("digit", ConfigItem.Type.INTEGER, 3L, 10L, null, null, true, "digit")
                        .addDateAttribute("data", null, false, "date field"), 1L, 5L, true, "test array")
                .build(true);
        ConfigSwaggerConverter converter = new ConfigSwaggerConverter();

        Assert.assertArrayEquals("Should generate 1 paths.",
                new String[]{"/rest/cm/configName/default"}, openapi.getPaths().keySet().toArray());
        Assert.assertEquals("Should generate operations for /rest/cm/configName/default.",
                2, openapi.getPaths().get("/rest/cm/configName/default").readOperations().size());
        Assert.assertArrayEquals("Should generate 2 schemas.",
                new String[]{elementName, "arr1"}, openapi.getComponents().getSchemas().keySet().toArray());
        ObjectSchema schema = (ObjectSchema) openapi.getComponents().getSchemas().get(elementName);
        Assert.assertEquals("Should generate properties.", 2, schema.getProperties().size());
        Assert.assertEquals("Should generate att1.", "date-time", schema.getProperties().get("att1").getFormat());
        ArraySchema array = (ArraySchema) schema.getProperties().get("arr1");
        Assert.assertEquals("Should generate arr1 min item.", (Integer) 1, array.getMinItems());
        Assert.assertEquals("Should generate arr1 max item.", (Integer) 5, array.getMaxItems());
        Assert.assertEquals("Should have correct arr1 data type.", "#/components/schemas/arr1", array.getItems().get$ref());
        ObjectSchema schemaArr1 = (ObjectSchema) openapi.getComponents().getSchemas().get("arr1");
        Assert.assertEquals("Should generate arr1 schemas.", 3, schemaArr1.getProperties().size());
    }

    @Test(expected = RuntimeException.class)
    public void testDuplicateAttributeName() {
        OpenAPIBuilder.createBuilder(configName, elementName, ConfigurationManagerService.BASE_PATH)
                .addDateTimeAttribute("att1", null, true, "date time doc")
                .addDateAttribute("att1", null, true, "date doc");
    }

    @Test
    public void testConvertBack() throws JsonProcessingException {
        OpenAPIBuilder builder = OpenAPIBuilder.createBuilder(configName, elementName, ConfigurationManagerService.BASE_PATH)
                .addDateTimeAttribute("att1", null, true, "date time doc")
                .addDateTimeAttribute("att2", null, true, "date time doc")
                .addObject("obj1", OpenAPIBuilder.createBuilder()
                        .addStringAttribute("name", 3L, 10L, "[A-Z]*", null, true, "val1")
                        .addNumberAttribute("digit", ConfigItem.Type.INTEGER, 3L, 10L, null, null, true, "digit")
                        .addDateAttribute("data", null, false, "date field"), true, "test object")
                .addArray("arr1", ConfigItem.Type.INTEGER, null, 100L, 20L, null, 10L, null, null, true, "test array")
                .addArray("arr2", OpenAPIBuilder.createBuilder()
                        .addStringAttribute("name", 3L, 10L, null, null, true, "val1")
                        .addNumberAttribute("digit", ConfigItem.Type.INTEGER, 3L, 10L, null, null, true, "digit")
                        .addDateAttribute("data", null, false, "date field"), 1L, 5L, true, "test array");
        OpenAPI openapi = builder.build(false);
        OpenAPIBuilder newBuilder = OpenAPIBuilder.createBuilder(configName, elementName, ConfigurationManagerService.BASE_PATH, openapi);
        OpenAPI newOpenapi = newBuilder.build(false);

        ConfigSwaggerConverter converter = new ConfigSwaggerConverter();
        String json = converter.convertOpenAPIToString(openapi, "application/json");
        String newJson = converter.convertOpenAPIToString(newOpenapi, "application/json");

        JSONAssert.assertEquals(new JSONObject(json), new JSONObject(newJson), true);
    }

    @Test
    public void testOpenAPIAddAttribute() {
        OpenAPI openapi = OpenAPIBuilder.createBuilder(configName, elementName, ConfigurationManagerService.BASE_PATH)
                .addStringAttribute("att1", null, null, "^\\d{3}-\\d{3}$", "val1", true, "att1 doc")
                .addStringAttribute("att2", null, null, "^\\d{3}-\\d{3}$", "val2", true, "att2 doc")
                .build(false);

        OpenAPI newOpenapi = OpenAPIBuilder.createBuilder(configName, elementName, ConfigurationManagerService.BASE_PATH, openapi)
                .addStringAttribute("att3", null, null, "^\\d{3}-\\d{3}$", "val2", true, "att2 doc")
                .build(false);

        Assert.assertEquals("Should able to add new attribute!", newOpenapi.getComponents().getSchemas().get(elementName).getProperties().size(), 3);
    }

    @Test
    public void testOpenAPIRemoveAttribute() throws JsonProcessingException {
        OpenAPI openapi = OpenAPIBuilder.createBuilder(configName, elementName, ConfigurationManagerService.BASE_PATH)
                .addStringAttribute("att1", null, null, "^\\d{3}-\\d{3}$", "val1", true, "att1 doc")
                .addStringAttribute("att2", null, null, "^\\d{3}-\\d{3}$", "val2", true, "att2 doc")
                .build(false);

        OpenAPI newOpenapi = OpenAPIBuilder.createBuilder(configName, elementName, ConfigurationManagerService.BASE_PATH, openapi)
                .removeAttribute("att2")
                .build(false);

        Assert.assertEquals("Should able to add new attribute!", newOpenapi.getComponents().getSchemas().get(elementName).getProperties().size(), 1);
    }

}
