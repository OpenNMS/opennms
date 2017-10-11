/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

    public TriggerPackage getTriggerPackageForCategoryFilter(final String categoryFilter) {
        for (final TriggerPackage triggerPackage : triggerPackages) {
            if (categoryFilter.equals(triggerPackage.getCategoryFilter())) {
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
