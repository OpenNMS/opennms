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
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import org.opennms.netmgt.model.OnmsSeverity;

import java.util.Map;
import java.util.Set;

public class GraphMLEdgeStatus extends GraphMLStatus {

    private GraphMLEdgeStatus(final OnmsSeverity severity,
                              final Map<String, String> styleProperties) {
        super(severity, styleProperties);
    }

    public GraphMLEdgeStatus() {
        super(OnmsSeverity.NORMAL);
    }

    @Override
    public Set<String> getAllowedStyleProperties() {
        return ImmutableSet.of("stroke",
                               "stroke-width",
                               "stroke-opacity",
                               "stroke-dasharray");
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof GraphMLEdgeStatus)) {
            return false;
        }

        final GraphMLEdgeStatus that = (GraphMLEdgeStatus) o;
        return Objects.equal(this.getSeverity(), that.getSeverity()) &&
               Objects.equal(this.getStyleProperties(), that.getStyleProperties());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getSeverity(),
                                this.getStyleProperties());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("severity", this.getSeverity())
                          .add("styleProperties", this.getStyleProperties())
                          .toString();
    }

    public static GraphMLEdgeStatus merge(final GraphMLEdgeStatus s1,
                                          final GraphMLEdgeStatus s2) {
        return new GraphMLEdgeStatus(GraphMLStatus.mergeSeverity(s1, s2),
                                     GraphMLStatus.mergeStyleProperties(s1, s2));
    }
}
