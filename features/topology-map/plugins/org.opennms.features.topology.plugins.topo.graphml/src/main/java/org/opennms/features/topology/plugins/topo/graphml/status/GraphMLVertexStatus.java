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
package org.opennms.features.topology.plugins.topo.graphml.status;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import org.opennms.netmgt.model.OnmsSeverity;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class GraphMLVertexStatus extends GraphMLStatus {
    private long alarmCount;

    public GraphMLVertexStatus(final OnmsSeverity severity,
                               final long alarmCount,
                               final Map<String, String> styleProperties) {
        super(severity, styleProperties);
        this.alarmCount = alarmCount;
    }

    public GraphMLVertexStatus(final OnmsSeverity severity,
                               final long alarmCount) {
        super(severity);
        this.alarmCount = alarmCount;
    }

    public GraphMLVertexStatus() {
        super(OnmsSeverity.NORMAL);
    }

    @Override
    public Set<String> getAllowedStyleProperties() {
        return Collections.emptySet();
    }

    public long getAlarmCount() {
        return this.alarmCount;
    }

    public final GraphMLStatus alarmCount(final long alarmCount) {
        this.alarmCount = alarmCount;
        return this;
    }

    @Override
    public Map<String, String> getStatusProperties() {
        return ImmutableMap.<String, String>builder()
                .putAll(super.getStatusProperties())
                .put("statusCount", Long.toString(alarmCount))
                .build();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof GraphMLVertexStatus)) {
            return false;
        }

        final GraphMLVertexStatus that = (GraphMLVertexStatus) o;
        return com.google.common.base.Objects.equal(this.getSeverity(), that.getSeverity()) &&
               com.google.common.base.Objects.equal(this.getAlarmCount(), that.getAlarmCount()) &&
               com.google.common.base.Objects.equal(this.getStyleProperties(), that.getStyleProperties());
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(this.getSeverity(),
                                                       this.getAlarmCount(),
                                                       this.getStyleProperties());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("severity", this.getSeverity())
                          .add("alarmCount", this.getAlarmCount())
                          .add("styleProperties", this.getStyleProperties())
                          .toString();
    }

    public static GraphMLVertexStatus merge(final GraphMLVertexStatus s1,
                                            final GraphMLVertexStatus s2) {
        return new GraphMLVertexStatus(GraphMLStatus.mergeSeverity(s1, s2),
                                       s1.alarmCount + s2.alarmCount,
                                       GraphMLStatus.mergeStyleProperties(s1, s2));
    }

}
