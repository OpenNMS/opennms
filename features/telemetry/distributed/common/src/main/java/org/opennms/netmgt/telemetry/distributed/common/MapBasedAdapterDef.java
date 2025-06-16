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
package org.opennms.netmgt.telemetry.distributed.common;

import com.google.common.collect.Lists;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.config.api.PackageDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MapBasedAdapterDef implements AdapterDefinition {
    private final String queueName;

    private final String name;
    private final String className;
    private final Map<String, String> parameters;

    public MapBasedAdapterDef(final String queueName, final PropertyTree definition) {
        this.queueName = queueName;
        this.name = definition.getRequiredString("name");
        this.className = definition.getRequiredString("class-name");
        this.parameters = definition.getFlatMap("parameters");
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
    public String getFullName() {
        return String.format("%s.%s", this.queueName, this.getName());
    }

    @Override
    public List<? extends PackageDefinition> getPackages() {
        try (InputStream inputStream = getClass().getResourceAsStream("/package.xml")) {
            final org.opennms.netmgt.telemetry.config.model.PackageConfig pkg = JaxbUtils.unmarshal(org.opennms.netmgt.telemetry.config.model.PackageConfig.class, inputStream);
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
