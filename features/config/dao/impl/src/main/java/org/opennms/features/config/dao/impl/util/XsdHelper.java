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
package org.opennms.features.config.dao.impl.util;

import com.google.common.io.Resources;
import io.swagger.v3.oas.models.OpenAPI;
import org.opennms.features.config.dao.api.ConfigConverter;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.api.ConfigItem;
import org.opennms.features.config.exception.SchemaConversionException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Main helper class for all xsd related function
 */
public class XsdHelper {
    /**
     * It will search xsds first, otherwise it will search across classpath
     *
     * @return URL of the xsd file
     */
    public static URL getSchemaPath(String xsdName) throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("xsds/" + xsdName);
        if (url == null) {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath*:**/" + xsdName);
            if (resources != null && resources.length > 0) {
                url = resources[0].getURL();
            }
        }
        return url;
    }

    /**
     * Convert xsd into openapi spec
     *
     * @param xsdName
     * @return
     */
    private static XsdModelConverter getXsdModelConverter(String xsdName) {
        Assert.notNull(xsdName);
        try {
            String xsdStr = Resources.toString(XsdHelper.getSchemaPath(xsdName), StandardCharsets.UTF_8);
            return new XsdModelConverter(xsdStr);
        } catch (IOException e) {
            throw new SchemaConversionException(xsdName, e);
        }
    }

    /**
     * It help to convert xsd to openapi and prepare all metadata needed
     *
     * @param configName
     * @param xsdName
     * @param topLevelElement
     * @return ConfigDefinition
     */
    public static ConfigDefinition buildConfigDefinition(String configName, String xsdName, String topLevelElement, String basePath){
        return buildConfigDefinition(configName, xsdName, topLevelElement, basePath, false);
    }

    /**
     * It help to convert xsd to openapi and prepare all metadata needed
     * @param configName
     * @param xsdName
     * @param topLevelElement
     * @param basePath
     * @param allowMultiple
     * @return ConfigDefinition
     */
    public static ConfigDefinition buildConfigDefinition(String configName, String xsdName, String topLevelElement, String basePath, boolean allowMultiple) {
        ConfigDefinition def = new ConfigDefinition(configName, allowMultiple);
        XsdModelConverter xsdConverter = XsdHelper.getXsdModelConverter(xsdName);
        ConfigItem item = xsdConverter.convert(topLevelElement);

        ConfigSwaggerConverter swaggerConverter = new ConfigSwaggerConverter();
        OpenAPI api = swaggerConverter.convert(item, basePath + "/" + configName);

        def.setSchema(api);
        def.setMetaValue(ConfigDefinition.TOP_LEVEL_ELEMENT_NAME_TAG, topLevelElement);
        def.setMetaValue(ConfigDefinition.XSD_FILENAME_TAG, xsdName);
        def.setMetaValue(ConfigDefinition.ELEMENT_NAME_TO_VALUE_NAME_TAG, xsdConverter.getElementNameToValueNameMap());

        return def;
    }

    /**
     * Build XmlConverter from ConfigDefinition
     *
     * @param def
     * @return
     * @throws IOException
     */
    public static ConfigConverter getConverter(ConfigDefinition def) throws IOException {
        String xsdName = (String) def.getMetaValue(ConfigDefinition.XSD_FILENAME_TAG);
        String topLevelElement = (String) def.getMetaValue(ConfigDefinition.TOP_LEVEL_ELEMENT_NAME_TAG);
        Map<String, String> elementNameToValueNameMap = (Map) def.getMetaValue(ConfigDefinition.ELEMENT_NAME_TO_VALUE_NAME_TAG);
        if (xsdName == null || topLevelElement == null) {
            throw new SchemaConversionException("ConfigDefinition " + def.getConfigName() + " NOT support XmlConverter.");
        }
        return new JaxbXmlConverter(xsdName, topLevelElement, elementNameToValueNameMap);
    }
}
