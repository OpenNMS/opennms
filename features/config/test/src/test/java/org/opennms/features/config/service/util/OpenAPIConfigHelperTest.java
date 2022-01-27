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
