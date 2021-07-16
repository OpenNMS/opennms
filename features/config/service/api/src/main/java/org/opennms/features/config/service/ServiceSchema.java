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

package org.opennms.features.config.service;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class ServiceSchema {
    private final XMLSchema xmlSchema;
    private final ConfigItem configItem;

    @JsonCreator
    public ServiceSchema(@JsonProperty("xmlSchema") final XMLSchema xmlSchema, @JsonProperty("configItem") final ConfigItem configItem) {
        this.xmlSchema = Objects.requireNonNull(xmlSchema);
        this.configItem = Objects.requireNonNull(configItem);
    }

    public XMLSchema getXmlSchema() {
        return xmlSchema;
    }

    public ConfigItem getConfigItem() {
        return configItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceSchema that = (ServiceSchema) o;
        return Objects.equals(xmlSchema, that.xmlSchema) && Objects.equals(configItem, that.configItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xmlSchema, configItem);
    }
}
