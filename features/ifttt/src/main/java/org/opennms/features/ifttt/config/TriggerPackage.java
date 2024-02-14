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

import com.google.common.base.Strings;

public class TriggerPackage {
    /**
     * category filter
     */
    private String categoryFilter = "";
    /**
     * uei filter
     */
    private String reductionKeyFilter = "";
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
        return categoryFilter;
    }

    public void setCategoryFilter(final String categoryFilter) {
        this.categoryFilter = categoryFilter;
    }

    @XmlAttribute
    public String getReductionKeyFilter() {
        return reductionKeyFilter;
    }

    public void setReductionKeyFilter(final String reductionKeyFilter) {
        this.reductionKeyFilter = reductionKeyFilter;
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
                Objects.equals(reductionKeyFilter, that.reductionKeyFilter) &&
                Objects.equals(triggerSets, that.triggerSets) &&
                Objects.equals(onlyUnacknowledged, that.onlyUnacknowledged);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryFilter, reductionKeyFilter, triggerSets, onlyUnacknowledged);
    }

    public String getFilterKey() {
        return getCategoryFilter() + " / " + getReductionKeyFilter();
    }

    @Override
    public String toString() {
        return "TriggerPackage{" +
                "categoryFilter='" + categoryFilter + '\'' +
                ", reductionKeyFilter='" + reductionKeyFilter + '\'' +
                ", triggerSets=" + triggerSets +
                ", onlyUnacknowledged=" + onlyUnacknowledged +
                '}';
    }
}
