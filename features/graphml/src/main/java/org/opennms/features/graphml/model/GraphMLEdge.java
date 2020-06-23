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

package org.opennms.features.graphml.model;

import java.util.Objects;

public class GraphMLEdge extends GraphMLElement {

    private GraphMLNode target;
    private GraphMLNode source;

    public GraphMLNode getTarget() {
        return target;
    }

    public GraphMLNode getSource() {
        return source;
    }

    public void setTarget(GraphMLNode target) {
        this.target = Objects.requireNonNull(target);
    }

    public void setSource(GraphMLNode source) {
        this.source = Objects.requireNonNull(source);
    }

    @Override
    public <T> T accept(GraphMLElementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), target, source);
    }

    @Override
    public boolean equals(Object obj) {
        boolean equals = super.equals(obj);
        if (equals) {
            if (obj instanceof GraphMLEdge) {
                GraphMLEdge other = (GraphMLEdge) obj;
                equals = Objects.equals(target, other.target)
                        && Objects.equals(source, other.source);
                return equals;
            }
        }
        return false;
    }
}
