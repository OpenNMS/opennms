/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2021 The OpenNMS Group, Inc.
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

import io.swagger.v3.oas.models.OpenAPI;
import org.opennms.features.config.dao.api.ConfigItem;

import java.util.HashSet;
import java.util.Set;

public class OpenAPIBuilder {
    private ConfigItem rootConfig;
    private String configName;
    private String topElementName;
    private String prefix;

    Set<String> usedAttributeNames = new HashSet<>();

    /**
     * In most cases, configName and topElementName can be the same. It just gives you the flexibility.
     * If you only want to make a nested object use createBuilder()
     * More details of usage please refer to OpenAPIBuilderTest.class
     * @param name
     * @param topElementName
     * @param prefix
     * @return
     * @see #createBuilder()
     * @see org.opennms.features.config.dao.util.OpenAPIBuilderTest
     */
    public static OpenAPIBuilder createBuilder(String name, String topElementName, String prefix) {
        OpenAPIBuilder builder = new OpenAPIBuilder();
        builder.configName = name;
        builder.topElementName = topElementName;
        builder.prefix = prefix;
        builder.rootConfig = new ConfigItem();
        builder.rootConfig.setName(topElementName);
        builder.rootConfig.setType(ConfigItem.Type.OBJECT);
        return builder;
    }

    public static OpenAPIBuilder createBuilder() {
        return OpenAPIBuilder.createBuilder(null, null, null);
    }

    public OpenAPI build(boolean isSingleConfig) {
        ConfigSwaggerConverter converter = new ConfigSwaggerConverter();
        return converter.convert(rootConfig, prefix + "/" + configName, isSingleConfig);
    }

    public OpenAPIBuilder addAttribute(ConfigItem configItem) {
        rootConfig.getChildren().add(configItem);
        return this;
    }

    public OpenAPIBuilder addArray(String name, ConfigItem.Type elementType, Long arrayMin, Long arrayMax, Long min, Long max, Long multipleOf,
                                   String pattern, Object defaultValue, boolean required, String doc) {
        ConfigItem arrayItem = this.getConfigItem(name, ConfigItem.Type.ARRAY, arrayMin, arrayMax, null, null, null, required, doc);
        ConfigItem children = this.getConfigItem(null, elementType, min, max, multipleOf, pattern, defaultValue, required, doc);
        arrayItem.getChildren().add(children);
        rootConfig.getChildren().add(arrayItem);
        return this;
    }

    public OpenAPIBuilder addArray(String name, OpenAPIBuilder childrenBuilder, Long arrayMin, Long arrayMax, boolean required, String doc) {
        ConfigItem arrayItem = this.getConfigItem(name, ConfigItem.Type.ARRAY, arrayMin, arrayMax, null, null, null, required, doc);
        childrenBuilder.rootConfig.setName(name);
        arrayItem.getChildren().add(childrenBuilder.rootConfig);
        rootConfig.getChildren().add(arrayItem);
        return this;
    }

    public OpenAPIBuilder addObject(String name, OpenAPIBuilder childrenBuilder, boolean required, String doc) {
        ConfigItem objectItem = this.getConfigItem(name, ConfigItem.Type.OBJECT, null, null, null, null, null, required, doc);
        objectItem.getChildren().add(childrenBuilder.rootConfig);
        rootConfig.getChildren().add(objectItem);
        return this;
    }

    public OpenAPIBuilder addBooleanAttribute(String name, Object defaultValue, boolean required, String doc) {
        return this.addAttribute(name, ConfigItem.Type.BOOLEAN, null, null, null, null, defaultValue, required, doc);
    }

    public OpenAPIBuilder addDateTimeAttribute(String name, Object defaultValue, boolean required, String doc) {
        return this.addAttribute(name, ConfigItem.Type.DATE_TIME, null, null, null, null, defaultValue, required, doc);
    }

    public OpenAPIBuilder addDateAttribute(String name, Object defaultValue, boolean required, String doc) {
        return this.addAttribute(name, ConfigItem.Type.DATE, null, null, null, null, defaultValue, required, doc);
    }

    public OpenAPIBuilder addStringAttribute(String name, Long minLength, Long maxLength,
                                             String pattern, Object defaultValue, boolean required, String doc) {
        return this.addAttribute(name, ConfigItem.Type.STRING, minLength, maxLength, null, pattern, defaultValue, required, doc);
    }

    public OpenAPIBuilder addNumberAttribute(String name, ConfigItem.Type type, Long min, Long max,
                                             Long multipleOf, Object defaultValue, boolean required, String doc) {
        if (type != ConfigItem.Type.NUMBER && type != ConfigItem.Type.INTEGER && type != ConfigItem.Type.LONG) {
            throw new RuntimeException("Type should be NUMBER/INTEGER/LONG");
        }
        return this.addAttribute(name, type, min, max, multipleOf, null, defaultValue, required, doc);
    }

    private OpenAPIBuilder addAttribute(String name, ConfigItem.Type type, Long min, Long max, Long multipleOf,
                                        String pattern, Object defaultValue, boolean required, String doc) {
        return this.addAttribute(
                this.getConfigItem(name, type, min, max, multipleOf, pattern, defaultValue, required, doc));
    }

    private ConfigItem getConfigItem(String name, ConfigItem.Type type, Long min, Long max, Long multipleOf,
                                     String pattern, Object defaultValue, boolean required, String doc) {
        if (usedAttributeNames.contains(name)) {
            throw new RuntimeException("Duplicated attribute name exist! name = " + name);
        }
        usedAttributeNames.add(name);
        ConfigItem configItem = new ConfigItem();
        configItem.setType(type);
        configItem.setName(name);
        if (max != null)
            configItem.setMax(max);
        if (min != null)
            configItem.setMin(min);
        if (multipleOf != null)
            configItem.setMultipleOf(multipleOf);
        if (pattern != null)
            configItem.setPattern(pattern);
        if (defaultValue != null)
            configItem.setDefaultValue(defaultValue);
        configItem.setRequired(required);
        if (doc != doc)
            configItem.setDocumentation(doc);
        return configItem;
    }
}