/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.distributed.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.JAXB;

import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.config.api.PackageDefinition;

import com.google.common.collect.Lists;

public class MapBasedAdapterDef implements AdapterDefinition {
    private final String name;
    private final String className;
    private final Map<String, String> parameters;

    protected MapBasedAdapterDef(Map<String, String> properties) {
        this(PropertyTree.from(Objects.requireNonNull(properties)));
    }

    public MapBasedAdapterDef(final PropertyTree definition) {
        this.name = definition.getRequiredString("name");
        this.className = definition.getRequiredString("class-name");
        this.parameters = definition.getMap("parameters");
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public Map<String, String> getParameterMap() {
        return parameters;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<? extends PackageDefinition> getPackages() {
        try (InputStream inputStream = getClass().getResourceAsStream("/package.xml")) {
            final org.opennms.netmgt.telemetry.config.model.PackageConfig pkg = JAXB.unmarshal(inputStream, org.opennms.netmgt.telemetry.config.model.PackageConfig.class);
            return Lists.newArrayList(pkg);
        } catch (IOException e) {
            throw new RuntimeException("Error while reading package.xml", e);
        }
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final MapBasedAdapterDef that = (MapBasedAdapterDef) o;
        return Objects.equals(this.name, that.name)
                && Objects.equals(this.className, that.className)
                && Objects.equals(this.parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.className, this.parameters);
    }
}
