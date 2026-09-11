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
package org.opennms.netmgt.availability;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * What the availability scheduler needs to know about one category: its
 * label, the filter rule selecting its nodes, and the service names it covers.
 */
public final class CategoryDefinition {
    private final String m_label;
    private final String m_rule;
    private final List<String> m_services;
    private final Double m_normalThreshold;
    private final Double m_warningThreshold;

    public CategoryDefinition(final String label, final String rule, final List<String> services) {
        this(label, rule, services, null, null);
    }

    public CategoryDefinition(final String label, final String rule, final List<String> services, final Double normalThreshold, final Double warningThreshold) {
        m_label = Objects.requireNonNull(label, "label");
        m_rule = Objects.requireNonNull(rule, "rule");
        m_services = services == null ? Collections.emptyList() : Collections.unmodifiableList(services);
        m_normalThreshold = normalThreshold;
        m_warningThreshold = warningThreshold;
    }

    public String getLabel() {
        return m_label;
    }

    /** The effective filter rule, already combined with the group's common rule. */
    public String getRule() {
        return m_rule;
    }

    /** Service names to cover; empty means every service. */
    public List<String> getServices() {
        return m_services;
    }

    /** Availability at or above which the category is normal, or null when not configured. */
    public Double getNormalThreshold() {
        return m_normalThreshold;
    }

    /** Availability below which the category is in trouble, or null when not configured. */
    public Double getWarningThreshold() {
        return m_warningThreshold;
    }

    @Override
    public String toString() {
        return "CategoryDefinition[" + m_label + ", rule=" + m_rule + ", services=" + m_services
                + ", normal=" + m_normalThreshold + ", warning=" + m_warningThreshold + "]";
    }
}
