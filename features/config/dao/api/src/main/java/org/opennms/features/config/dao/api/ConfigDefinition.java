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

/**
 * TODO Patrick: Discuss with Freddy
 */

import com.atlassian.oai.validator.report.ValidationReport;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.models.OpenAPI;
import org.opennms.features.config.dao.api.util.OpenAPIDeserializer;
import org.opennms.features.config.dao.api.util.OpenAPISerializer;

import java.io.IOException;

/**
 * This class holds the definition for a configuration.
 * It is based on OpenAPI plus Metadata.
 */

public class ConfigDefinition {
    public enum TYPE {XML, PROPERTY}

    protected String configName;
    protected int maxInstances = 1;

    @JsonSerialize(using = OpenAPISerializer.class)
    @JsonDeserialize(using = OpenAPIDeserializer.class)
    protected OpenAPI schema;
    protected TYPE type;

    @JsonCreator
    public ConfigDefinition(@JsonProperty("configName") String configName) {
        this.configName = configName;
        type = TYPE.PROPERTY;
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

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    @JsonIgnore
    public ConfigConverter getConverter() throws IOException {
        throw new RuntimeException("getConverter NOT IMPLEMENT YET");
    }

    @JsonIgnore
    public ValidationReport validate(String json) {
        throw new RuntimeException("validate NOT IMPLEMENT YET");
    }
}
