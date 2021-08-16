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

package org.opennms.features.config.dao.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.IOException;
import java.net.URL;

public interface ConfigConverter<CONFIG_CLASS> {
    enum SCHEMA_TYPE {XML, JSON, PROPERTY};

    boolean validate(CONFIG_CLASS obj);

    /**
     * convert xml into config object
     *
     * @param xmlStr input xml string
     * @return configClass instance
     */
    CONFIG_CLASS xmlToJaxbObject(String xmlStr);

    /**
     * convert xml string to json string
     *
     * @param xmlStr input xml string
     * @return json string
     */
    String xmlToJson(String xmlStr);

    /**
     * convert json string to xml string
     *
     * @param jsonStr input json config
     * @return xml string
     */
    String jsonToXml(String jsonStr);

    /**
     * convert json string to config object
     *
     * @param jsonStr input json config
     * @return config object
     */
    CONFIG_CLASS jsonToJaxbObject(String jsonStr);


    /**
     * convert config object to json string
     *
     * @param configObject
     * @return json string
     */
    String jaxbObjectToJson(CONFIG_CLASS configObject);

    /**
     * convert config object to xml string
     *
     * @param configObject
     * @return json string
     */
    String jaxbObjectToXml(CONFIG_CLASS configObject);

    /**
     * get the configuration class register with the converter
     *
     * @return configuration class
     */
    Class<CONFIG_CLASS> getConfigurationClass();

    /**
     * @return ServiceSchema
     */
    ValidationSchema<?> getValidationSchema();

    /**
     * @return schema URL
     * @throws IOException
     */
    @JsonIgnore
    URL getSchemaPath() throws IOException;

    /**
     * @return schema type
     */
    @JsonIgnore
    SCHEMA_TYPE getSchemaType();

    /**
     * @return raw content of schema
     */
    @JsonIgnore
    String getRawSchema();
}