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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.config.dao.api.ConfigItem;
import org.opennms.features.config.dao.api.ConfigSchema;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath*:/META-INF/opennms/applicationContext-config-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class ConfigSwaggerConverterAllTest {
    public static final String CONFIG_NAME = "vacuumd";
    public static final String XSD_PATH = "vacuumd-configuration.xsd";
    public static final String XSD2_PATH = "xsds/vacuumd-configuration2.xsd";
    public static final String TOP_ELEMENT = "VacuumdConfiguration";

    @Autowired
    private ConfigurationManagerService configurationManagerService;

    @Before
    public void init() throws IOException, JAXBException {
        configurationManagerService.registerSchema(CONFIG_NAME, XSD_PATH, TOP_ELEMENT);
        configurationManagerService.registerSchema(CONFIG_NAME + "2", XSD2_PATH, TOP_ELEMENT);
    }

    @Test
    public void canConvertAllXsd() throws IOException {
        ConfigSwaggerConverter configSwaggerConverter = new ConfigSwaggerConverter();
        Map<String, ConfigSchema<?>> schemas = configurationManagerService.getAllConfigSchema();
        Map<String, ConfigItem> items = new HashMap<>();
        schemas.forEach((key, schema) -> {
            items.put(key, schema.getConverter().getValidationSchema().getConfigItem());
        });

        OpenAPI openapi = configSwaggerConverter.convert("/opennms/rest/cm/schema/", items);
        String yaml = configSwaggerConverter.convertOpenAPIToString(openapi, "application/yaml");

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {};
        Map<String, Object> map = mapper.readValue(yaml, typeRef);

        Assert.assertEquals("It should have empty components.", ((Map) map.get("components")).size(), 0);
        Map<String, Map> paths = (Map) map.get("paths");
        Assert.assertEquals("It should have 4 paths.", paths.size(), 4);
        Map<String,Map> getVaccuumdContent = (Map)((Map)((Map)((Map)paths.get("/opennms/rest/cm/schema/vacuumd/{configId}")
                .get("get")).get("responses")).get("200")).get("content");
        Map<String,Map> getVaccuumd2Content = (Map)((Map)((Map)((Map)paths.get("/opennms/rest/cm/schema/vacuumd2/{configId}")
                .get("get")).get("responses")).get("200")).get("content");
        String ref = (String) ((Map)getVaccuumdContent.get("application/json").get("schema")).get("$ref");
        String ref2 = (String) ((Map)getVaccuumd2Content.get("application/json").get("schema")).get("$ref");
        Assert.assertEquals("It should have correct ref.",
                "/opennms/rest/cm/schema/vacuumd#/components/schemas/VacuumdConfiguration", ref);
        Assert.assertEquals("It should have correct ref2.",
                "/opennms/rest/cm/schema/vacuumd2#/components/schemas/VacuumdConfiguration", ref2);
    }
}
