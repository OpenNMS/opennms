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

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class include meta-data of the config
 */
public class ConfigSchema<T extends ConfigConverter> implements Serializable {
    private String name;
    private Class<T> converterClass;
    private T converter;

    @JsonCreator
    public ConfigSchema(@JsonProperty("name") String name,
                        @JsonProperty("converterClass") Class<T> converterClass, @JsonProperty("converter") T converter) {
        this.name = name;
        this.converterClass = converterClass;
        this.setConverter(converter);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<T> getConverterClass() {
        return converterClass;
    }

    public T getConverter() {
        return converter;
    }

    public void setConverter(T converter) {
        this.converterClass = (Class<T>) converter.getClass();
        this.converter = converter;
    }
}
