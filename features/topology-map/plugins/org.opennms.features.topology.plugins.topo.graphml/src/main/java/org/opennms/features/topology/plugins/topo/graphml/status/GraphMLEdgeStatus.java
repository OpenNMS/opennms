/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
