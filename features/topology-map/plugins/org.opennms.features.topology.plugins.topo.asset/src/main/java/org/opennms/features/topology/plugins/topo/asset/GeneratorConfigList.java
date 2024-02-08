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
package org.opennms.features.topology.plugins.topo.asset;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="configs")
@XmlAccessorType(XmlAccessType.NONE)
public class GeneratorConfigList {

    @XmlElement(name="config")
    private List<GeneratorConfig> configs = new ArrayList<>();

    // Required by JAXB
    public GeneratorConfigList() {

    }

    public GeneratorConfigList(List<GeneratorConfig> configs) {
        this.configs.clear();
        this.configs.addAll(configs);
    }

    public List<GeneratorConfig> getConfigs() {
        return configs;
    }

    public void setConfigs(List<GeneratorConfig> configs) {
        this.configs = configs;
    }

    public GeneratorConfig getConfig(String providerId) {
        return configs.stream()
                .filter(config -> config.getProviderId().equals(providerId))
                .findFirst()
                .orElse(null);
    }

    public void removeConfig(String providerId) {
        Optional.ofNullable(getConfig(providerId))
            .ifPresent(config -> configs.remove(config));
    }

    public void addConfig(GeneratorConfig newConfig) {
        final GeneratorConfig oldConfig = getConfig(newConfig.getProviderId());
        if (oldConfig != null) { // remove before adding when already existing
            configs.remove(oldConfig);
        }
        configs.add(newConfig);
    }

    public int size() {
        return configs.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof GeneratorConfigList) {
            GeneratorConfigList other = (GeneratorConfigList) obj;
            boolean equals = Objects.equals(configs, other.configs);
            return equals;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(configs);
    }

    @Override
    public String toString() {
        return "GeneratorConfigList [configs=" + configs + "]";
    }
}
