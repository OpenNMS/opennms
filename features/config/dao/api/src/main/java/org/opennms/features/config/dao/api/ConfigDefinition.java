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
 *******************************************************************************/

package org.opennms.features.config.dao.api;

import com.atlassian.oai.validator.report.MessageResolver;
import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.schema.SchemaValidator;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.opennms.features.config.dao.api.util.OpenAPIDeserializer;
import org.opennms.features.config.dao.api.util.OpenAPISerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * This class holds the definition for a configuration.
 * It is based on OpenAPI plus Metadata.
 */

public class ConfigDefinition {
    transient public static final String TOP_LEVEL_ELEMENT_NAME_TAG = "topLevelElement";
    transient public static final String XSD_FILENAME_TAG = "xsdFilename";

    private String configName;
    private int maxInstances = 1;
    private Map<String, String> meta = new HashMap<>();

    @JsonSerialize(using = OpenAPISerializer.class)
    @JsonDeserialize(using = OpenAPIDeserializer.class)
    protected OpenAPI schema;

    @JsonCreator
    public ConfigDefinition(@JsonProperty("configName") String configName) {
        this.configName = configName;
    }

    public OpenAPI getSchema() {
        return schema;
    }

    public void setSchema(OpenAPI schema) {
        this.schema = schema;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public int getMaxInstances() {
        return maxInstances;
    }

    public void setMaxInstances(int maxInstances) {
        this.maxInstances = maxInstances;
    }

    public Map<String, String> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, String> meta) {
        this.meta = meta;
    }

    public String getMetaValue(String key) {
        return meta.get(key);
    }

    public void setMetaValue(String key, String value) {
        this.meta.put(key, value);
    }

    @JsonIgnore
    public ValidationReport validate(String json) {
        String topSchemaName = meta.get(TOP_LEVEL_ELEMENT_NAME_TAG);
        if(topSchemaName == null) {
            topSchemaName = configName;
        }
        if(this.getSchema() == null){
            throw new RuntimeException("Empty schema!");
        }
        SchemaValidator validator = new SchemaValidator(this.getSchema(), new MessageResolver());
        final Schema schema = new Schema().$ref("#/components/schemas/" + topSchemaName);
        ValidationReport report = validator.validate(json, schema, null);
        return report;
    }
}
