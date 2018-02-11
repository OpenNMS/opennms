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

public class TriggerPackage {
    /**
     * category filter
     */
    private String categoryFilter;
    /**
     * trigger sets for firing IFTTT events
     */
    private List<TriggerSet> triggerSets = new ArrayList<>();
    /**
     * whether only unacknowledged alarms are retrieved
     */
    private Boolean onlyUnacknowledged = Boolean.TRUE;

    @XmlAttribute
    public String getCategoryFilter() {
        return categoryFilter != null ? categoryFilter : ".*";
    }

    public void setCategoryFilter(final String categoryFilter) {
        this.categoryFilter = categoryFilter;
    }

    @XmlAttribute
    public Boolean getOnlyUnacknowledged() {
        return onlyUnacknowledged;
    }

    public void setOnlyUnacknowledged(Boolean onlyUnacknowledged) {
        this.onlyUnacknowledged = onlyUnacknowledged;
    }

    @XmlElement(name = "trigger-set")
    public List<TriggerSet> getTriggerSets() {
        return triggerSets;
    }

    public void setTriggerSets(final List<TriggerSet> triggerSets) {
        this.triggerSets = triggerSets;
    }

    public TriggerSet getTriggerSetForName(final String name) {
        for (final TriggerSet triggerSet : triggerSets) {
            if (name.equalsIgnoreCase(triggerSet.getName())) {
                return triggerSet;
            }
        }
        return null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof TriggerPackage)) return false;

        final TriggerPackage that = (TriggerPackage) o;

        return Objects.equals(categoryFilter, that.categoryFilter) &&
               Objects.equals(triggerSets, that.triggerSets) &&
               Objects.equals(onlyUnacknowledged, that.onlyUnacknowledged);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryFilter, triggerSets, onlyUnacknowledged);
    }
}
