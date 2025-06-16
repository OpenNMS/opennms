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
package org.opennms.features.config.dao.impl.util;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.*;
import org.opennms.features.config.dao.api.ConfigItem;
import org.opennms.features.config.exception.SchemaConversionException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OpenAPIBuilder {
    public static final String SCHEMA_REF_TAG = "#/components/schemas/";
    private ConfigItem rootConfig;
    private String configName;
    private String topElementName;
    private String prefix;

    Set<String> usedAttributeNames = new HashSet<>();

    /**
     * In most cases, configName and topElementName can be the same. It just gives you the flexibility.
     * If you only want to make a nested object use createBuilder()
     * More details of usage please refer to OpenAPIBuilderTest.class
     *
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

    /**
     * This is use for modify existing OpenAPI object
     *
     * @param name
     * @param topElementName
     * @param prefix
     * @param openapi        (existing)
     * @return
     */
    public static OpenAPIBuilder createBuilder(String name, String topElementName, String prefix, OpenAPI openapi) {
        OpenAPIBuilder builder = OpenAPIBuilder.createBuilder(name, topElementName, prefix);
        if (openapi == null) {
            return builder;
        }
        Schema<?> schema = openapi.getComponents().getSchemas().get(topElementName);
        if (schema == null) {
            return builder;
        }

        schema.getProperties().forEach((k, s) ->
                builder.walkSchema(k, s, builder.rootConfig, schema.getRequired(), openapi)
        );
        return builder;
    }

    public ConfigItem getRootConfig() {
        return this.rootConfig;
    }

    /**
     * handle for object schema ref lookup and build children attributes
     *
     * @param schema
     * @param openapi
     * @param item
     */
    private void handle$ref(Schema<?> schema, OpenAPI openapi, ConfigItem item) {
        String refObjName = schema.get$ref().replaceFirst("^" + SCHEMA_REF_TAG, "");
        Schema<?> refObjSchema = openapi.getComponents().getSchemas().get(refObjName);
        if (refObjSchema.getDescription() != null) {
            item.setDocumentation(refObjSchema.getDescription());
        }
        if (refObjSchema != null && refObjSchema.getProperties() != null) {
            refObjSchema.getProperties().forEach((k, s) ->
                    this.walkSchema(k, s, item, refObjSchema.getRequired(), openapi)
            );
        }
    }

    /**
     * Walk through all attributes
     *
     * @param name
     * @param schema
     * @param currentItem
     * @param required
     * @param openapi
     */
    private void walkSchema(String name, Schema<?> schema, ConfigItem currentItem, List<String> required, OpenAPI openapi) {
        ConfigItem item = this.getConfigItem(name, schema, required);
        if (item.getType() == ConfigItem.Type.ARRAY) {
            Schema<?> childSchema = ((ArraySchema) schema).getItems();
            ConfigItem childrenItem = this.getConfigItem(name, childSchema, required);
            if (childrenItem.getType() == ConfigItem.Type.OBJECT
                    && (childSchema.get$ref() != null && childSchema.get$ref().startsWith(SCHEMA_REF_TAG))) {
                this.handle$ref(childSchema, openapi, childrenItem);
            }
            item.getChildren().add(childrenItem);

        }
        if (schema.get$ref() != null && schema.get$ref().startsWith(SCHEMA_REF_TAG)) {
            this.handle$ref(schema, openapi, item);
        }
        currentItem.getChildren().add(item);
    }

    /**
     * Convert openapi schema to ConfigItem
     *
     * @param schema
     * @param required
     * @return
     */
    private ConfigItem getConfigItem(String schemaKey, Schema<?> schema, List<String> required) {
        ConfigItem item = new ConfigItem();
        item.setName((schema.getName() != null) ? schema.getName() : schemaKey);
        if (required != null && required.contains(item.getName())) {
            item.setRequired(true);
        }
        this.usedAttributeNames.add(schema.getName());
        item.setDefaultValue(schema.getDefault());
        if (schema instanceof DateTimeSchema) {
            item.setType(ConfigItem.Type.DATE_TIME);
        } else if (schema instanceof StringSchema) {
            item.setType(ConfigItem.Type.STRING);
            if (schema.getMinLength() != null) {
                item.setMin(Long.valueOf(schema.getMinLength().longValue()));
            }
            if (schema.getMaxLength() != null) {
                item.setMax(Long.valueOf(schema.getMaxLength().longValue()));
            }
        } else if (schema instanceof ArraySchema) {
            item.setType(ConfigItem.Type.ARRAY);
            if (schema.getMinItems() != null) {
                item.setMin(Long.valueOf(schema.getMinItems()));
            }
            if (schema.getMaxItems() != null) {
                item.setMax(Long.valueOf(schema.getMaxItems()));
            }
        } else if (schema instanceof ObjectSchema) {
            item.setType(ConfigItem.Type.OBJECT);
        } else if (schema instanceof NumberSchema || schema instanceof IntegerSchema) {
            if ("int64".equals(schema.getFormat())) {
                item.setType(ConfigItem.Type.LONG);
            } else if (schema instanceof IntegerSchema) {
                item.setType(ConfigItem.Type.INTEGER);
            } else {
                item.setType(ConfigItem.Type.NUMBER);
            }
            if (schema.getMinimum() != null) {
                item.setMin(Long.valueOf(schema.getMinimum().longValue()));
            }
            if (schema.getMaximum() != null) {
                item.setMax(Long.valueOf(schema.getMaximum().longValue()));
            }
            if (schema.getMultipleOf() != null) {
                item.setMultipleOf(Long.valueOf(schema.getMultipleOf().longValue()));
            }
        } else if (schema instanceof BooleanSchema) {
            item.setType(ConfigItem.Type.BOOLEAN);
        } else if (schema instanceof DateSchema) {
            item.setType(ConfigItem.Type.DATE);
        } else if (schema instanceof Schema) {
            item.setType(ConfigItem.Type.OBJECT);
        }
        if (schema.getPattern() != null) {
            item.setPattern(schema.getPattern());
        }
        if (schema.getDescription() != null) {
            item.setDocumentation(schema.getDescription());
        }
        return item;
    }

    public static OpenAPIBuilder createBuilder() {
        return OpenAPIBuilder.createBuilder(null, null, null);
    }

    /**
     * build OpenAPI, if isSingleConfig is false. It will only generate API path for get and update config. (default)
     * NO add / delete config and list configIds
     *
     * @param isSingleConfig
     * @return
     */
    public OpenAPI build(boolean isSingleConfig) {
        ConfigSwaggerConverter converter = new ConfigSwaggerConverter();
        return converter.convert(rootConfig, prefix + "/" + configName, isSingleConfig);
    }

    /**
     * It will directly remove the first level attribute only
     *
     * @param attributeName
     * @return
     */
    public OpenAPIBuilder removeAttribute(String attributeName) {
        rootConfig.getChildren().removeIf(item -> attributeName.equals(item.getName()));
        return this;
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
        objectItem.getChildren().addAll(childrenBuilder.rootConfig.getChildren());
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
            throw new SchemaConversionException("Type should be NUMBER/INTEGER/LONG");
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
            throw new SchemaConversionException("Duplicated attribute name exist! name = " + name);
        }
        usedAttributeNames.add(name);
        ConfigItem configItem = new ConfigItem();
        configItem.setType(type);
        configItem.setName(name);
        if (max != null) {
            configItem.setMax(max);
        }
        if (min != null) {
            configItem.setMin(min);
        }
        if (multipleOf != null) {
            configItem.setMultipleOf(multipleOf);
        }
        if (pattern != null) {
            configItem.setPattern(pattern);
        }
        if (defaultValue != null) {
            configItem.setDefaultValue(defaultValue);
        }
        configItem.setRequired(required);
        if (doc != null) {
            configItem.setDocumentation(doc);
        }
        return configItem;
    }
}
