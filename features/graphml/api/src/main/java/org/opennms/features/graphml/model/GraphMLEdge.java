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
