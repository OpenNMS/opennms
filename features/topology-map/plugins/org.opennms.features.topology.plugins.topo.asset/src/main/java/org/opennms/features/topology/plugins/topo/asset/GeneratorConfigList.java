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
