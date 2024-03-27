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
package org.opennms.features.ifttt.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ifttt-config")
public class IfTttConfig {
    /**
     * IFTTT key of the webhook service
     */
    private String key;
    /**
     * enabled flag
     */
    private Boolean enabled;
    /**
     * trigger packages for defining trigger sets
     */
    private List<TriggerPackage> triggerPackages = new ArrayList<>();
    /**
     * poll interval
     */
    private Long pollInterval = 30L;

    public IfTttConfig() {
    }

    @XmlAttribute
    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    @XmlAttribute
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }


    @XmlAttribute
    public Long getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(final Long pollInterval) {
        this.pollInterval = pollInterval;
    }

    @XmlElement(name = "trigger-package")
    public List<TriggerPackage> getTriggerPackages() {
        return triggerPackages;
    }

    public void setTriggerPackages(final List<TriggerPackage> triggerPackages) {
        this.triggerPackages = triggerPackages;
    }

    public TriggerPackage getTriggerPackageForFilters(final String filterKey) {
        for (final TriggerPackage triggerPackage : triggerPackages) {
            if (filterKey.equals(triggerPackage.getFilterKey())) {
                return triggerPackage;
            }
        }
        return null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof IfTttConfig)) return false;

        final IfTttConfig that = (IfTttConfig) o;

        return Objects.equals(key, that.key) &&
                Objects.equals(enabled, that.enabled) &&
                Objects.equals(triggerPackages, that.triggerPackages) &&
                Objects.equals(pollInterval, that.pollInterval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, enabled, pollInterval, triggerPackages);
    }
}
