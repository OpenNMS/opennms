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
package org.opennms.features.config.dao.impl;

import com.atlassian.oai.validator.report.MessageResolver;
import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.schema.SchemaValidator;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.io.Resources;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.dao.api.ConfigConverter;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.api.ConfigItem;
import org.opennms.features.config.dao.impl.util.ConfigSwaggerConverter;
import org.opennms.features.config.dao.impl.util.SchemaUtil;
import org.opennms.features.config.dao.impl.util.XmlConverter;
import org.opennms.features.config.dao.impl.util.XsdModelConverter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * This config definition class is special for opennms xsd config use
 */
public class XmlConfigDefinition extends ConfigDefinition {

    private String xsdName;

    private String topLevelElement;
//
//    public XmlConfigDefinition(){};

    @JsonCreator
    public XmlConfigDefinition(@JsonProperty("configName") String configName, @JsonProperty("xsdName") String xsdName,
                               @JsonProperty("topLevelElement") String topLevelElement) {
        super(configName);
        this.type = TYPE.XML;
        this.xsdName = xsdName;
        this.topLevelElement = topLevelElement;
    }

    public String getXsdName() {
        return xsdName;
    }

    public void setXsdName(String xsdName) {
        this.xsdName = xsdName;
    }

    public String getTopLevelElement() {
        return topLevelElement;
    }

    public void setTopLevelElement(String topLevelElement) {
        this.topLevelElement = topLevelElement;
    }

    private void buildOpenAPI() {
        try {
            ConfigSwaggerConverter converter = new ConfigSwaggerConverter();
            XsdModelConverter xsdConverter = new XsdModelConverter();
            String xsdStr = Resources.toString(SchemaUtil.getSchemaPath(xsdName), StandardCharsets.UTF_8);
            XmlSchemaCollection collection = xsdConverter.convertToSchemaCollection(xsdStr);
            ConfigItem item = xsdConverter.convert(collection, this.getTopLevelElement());
            this.schema = converter.convert(item, "/rest/cm/" + this.getConfigName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OpenAPI getSchema() {
        if (this.schema == null) {
            this.buildOpenAPI();
        }
        return this.schema;
    }

    @Override
    @JsonIgnore
    public ConfigConverter getConverter() throws IOException {
        return new XmlConverter(this.getXsdName(), this.getTopLevelElement());
    }

    @Override
    @JsonIgnore
    public ValidationReport validate(String json) {
        SchemaValidator validator = new SchemaValidator(this.getSchema(), new MessageResolver());
        final Schema schema = new Schema().$ref("#/components/schemas/" + this.getTopLevelElement());
        ValidationReport report = validator.validate(json, schema, null);
        return report;
    }
}
