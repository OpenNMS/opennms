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
