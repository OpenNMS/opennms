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
import org.opennms.features.config.exception.SchemaNotFoundException;
import org.opennms.features.config.exception.ValidationException;

import java.util.HashMap;
import java.util.Map;

/**
 * This class holds the definition for a configuration.
 * It is based on OpenAPI plus Metadata.
 */

public class ConfigDefinition {
    public static final String TOP_LEVEL_ELEMENT_NAME_TAG = "topLevelElement";
    public static final String XSD_FILENAME_TAG = "xsdFilename";
    public static final String ELEMENT_NAME_TO_VALUE_NAME_TAG = "elementNameToValueName";
    public static final String DEFAULT_CONFIG_ID = "default";

    private String configName;
    private boolean allowMultiple;
    private Map<String, Object> meta = new HashMap<>();

    @JsonSerialize(using = OpenAPISerializer.class)
    @JsonDeserialize(using = OpenAPIDeserializer.class)
    protected OpenAPI schema;

    @JsonCreator
    public ConfigDefinition(@JsonProperty("configName") String configName,
                            @JsonProperty("allowMultiple") Boolean allowMultiple) {
        this.configName = configName;
        this.allowMultiple = allowMultiple == null ? false : allowMultiple;
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

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    public Object getMetaValue(String key) {
        return meta.get(key);
    }

    public void setMetaValue(String key, Object value) {
        this.meta.put(key, value);
    }

    public boolean getAllowMultiple() {
        return allowMultiple;
    }

    public void setAllowMultiple(boolean allowMultiple) {
        this.allowMultiple = allowMultiple;
    }

    @JsonIgnore
    public void validate(String json) {
        String topSchemaName = (String) meta.get(TOP_LEVEL_ELEMENT_NAME_TAG);
        if (topSchemaName == null) {
            topSchemaName = configName;
        }
        if (this.getSchema() == null) {
            throw new SchemaNotFoundException("Empty schema!");
        }
        SchemaValidator validator = new SchemaValidator(this.getSchema(), new MessageResolver());
        final Schema<?> topSchema = new Schema<>().$ref("#/components/schemas/" + topSchemaName);
        ValidationReport report = validator.validate(json, topSchema, null);
        if (report.hasErrors()) {
            throw new ValidationException(report);
        }
    }
}
