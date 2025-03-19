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
package org.opennms.netmgt.config.snmpmetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name = "snmp-metadata-config")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("snmp-metadata-adapter-configuration.xsd")
public class SnmpMetadataConfig {
    @XmlAttribute(name = "resultsBehavior")
    private String resultsBehavior = "replace";
    @XmlAttribute(name = "enabled")
    private Boolean enabled = false;

    @XmlElement(name = "config")
    private List<Config> configs = new ArrayList<>();

    public List<Config> getConfigs() {
        return configs;
    }

    public void setConfigs(List<Config> configs) {
        this.configs = configs;
    }

    public String getResultsBehavior() {
        return resultsBehavior;
    }

    public void setResultsBehavior(String resultsBehavior) {
        this.resultsBehavior = resultsBehavior;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "SnmpMetadataConfig{" +
                "resultsBehavior='" + resultsBehavior + '\'' +
                ", enabled=" + enabled +
                ", configs=" + configs +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SnmpMetadataConfig that = (SnmpMetadataConfig) o;
        return Objects.equals(resultsBehavior, that.resultsBehavior) && Objects.equals(enabled, that.enabled) && Objects.equals(configs, that.configs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resultsBehavior, enabled, configs);
    }
}

