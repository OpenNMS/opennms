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
package org.opennms.features.config.service.util;

import io.swagger.v3.oas.models.OpenAPI;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.config.dao.api.ConfigItem;
import org.opennms.features.config.dao.impl.util.OpenAPIBuilder;
import org.opennms.features.config.service.api.ConfigurationManagerService;

public class OpenAPIConfigHelperTest {
    String configName = "configName";
    String elementName = "element";

    @Test
    public void testFillingOpenAPI() {
        OpenAPI openapi = OpenAPIBuilder.createBuilder(configName, elementName, ConfigurationManagerService.BASE_PATH)
                .addStringAttribute("att1", null, null, null, "val1", true, "att1 doc")
                .addBooleanAttribute("bool1", false, false, "bool att")
                .addArray("arr1", ConfigItem.Type.INTEGER, null, 100L, 20L, null, 10L, null, null, true, "test array")
                .addObject("obj1", OpenAPIBuilder.createBuilder()
                        .addStringAttribute("name", 3L, 10L, null, "name", true, "val1")
                        .addNumberAttribute("digit", ConfigItem.Type.INTEGER, 3L, 10L, null, 1, true, "digit")
                        , true, "test array")
                .build(false);

        JSONObject emptyJson = new JSONObject();
        OpenAPIConfigHelper.fillWithDefaultValue(openapi, elementName, emptyJson);

        Assert.assertEquals("It should contain att1", "val1", emptyJson.get("att1"));
        Assert.assertEquals("It should contain bool1", false, emptyJson.get("bool1"));
        Assert.assertEquals("It should contain empty arr1", 0, ((JSONArray)emptyJson.get("arr1")).length());
        JSONObject subObject = (JSONObject)emptyJson.get("obj1");
        Assert.assertNotNull("It should contain obj1", subObject);
        Assert.assertEquals("obj1 should contain name", "name", subObject.get("name"));
        Assert.assertEquals("obj1 should contain digit", 1, subObject.get("digit"));
    }
}
