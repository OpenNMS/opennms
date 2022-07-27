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
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.api.ConfigItem;
import org.opennms.features.config.dao.impl.util.ConfigSwaggerConverter;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

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
    public void init() throws IOException {
        ConfigDefinition def = XsdHelper.buildConfigDefinition(CONFIG_NAME, XSD_PATH, TOP_ELEMENT,
                ConfigurationManagerService.BASE_PATH, false);
        ConfigDefinition def2 = XsdHelper.buildConfigDefinition(CONFIG_NAME + "2", XSD2_PATH, TOP_ELEMENT,
                ConfigurationManagerService.BASE_PATH, false);
        configurationManagerService.registerConfigDefinition(CONFIG_NAME, def);
        configurationManagerService.registerConfigDefinition(CONFIG_NAME + "2", def2);
    }

    @Test
    public void canConvertAllXsd() throws Exception {
        ConfigSwaggerConverter configSwaggerConverter = new ConfigSwaggerConverter();
        Map<String, ConfigDefinition> defs = configurationManagerService.getAllConfigDefinitions();
        Map<String, OpenAPI> apis = new HashMap<>(defs.size());
        defs.forEach((key, def) -> {
            OpenAPI api = def.getSchema();
            if (api != null && api.getPaths() != null) {
                apis.put(key, api);
            }
        });

        OpenAPI openapi = configSwaggerConverter.mergeAllPathsWithRemoteRef(apis, "/opennms/rest/cm");
        String yaml = configSwaggerConverter.convertOpenAPIToString(openapi, "application/yaml");

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {
        };
        Map<String, Object> map = mapper.readValue(yaml, typeRef);

        Assert.assertNull("It should have empty components.", (map.get("components")));
        Map<String, Map> paths = (Map) map.get("paths");
        Assert.assertEquals("It should have 4 paths.", paths.size(), 4);
        Map<String, Map> getVaccuumdContent = (Map) ((Map) ((Map) ((Map) paths.get("/rest/cm/vacuumd/{configId}")
                .get("get")).get("responses")).get("200")).get("content");
        Map<String, Map> getVaccuumd2Content = (Map) ((Map) ((Map) ((Map) paths.get("/rest/cm/vacuumd2/{configId}")
                .get("get")).get("responses")).get("200")).get("content");
        String ref = (String) ((Map) getVaccuumdContent.get("application/json").get("schema")).get("$ref");
        String ref2 = (String) ((Map) getVaccuumd2Content.get("application/json").get("schema")).get("$ref");
        Assert.assertEquals("It should have correct ref.",
                "/opennms/rest/cm/schema/vacuumd#/components/schemas/VacuumdConfiguration", ref);
        Assert.assertEquals("It should have correct ref2.",
                "/opennms/rest/cm/schema/vacuumd2#/components/schemas/VacuumdConfiguration", ref2);
    }
}
