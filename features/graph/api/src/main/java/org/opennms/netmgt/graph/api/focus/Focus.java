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
package org.opennms.netmgt.graph.api.focus;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.graph.api.VertexRef;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class Focus {

    private final String id;
    private final List<VertexRef> vertexRefs;

    public Focus(final String id) {
        this(id, Lists.newArrayList());
    }

    public Focus(final String id, final List<VertexRef> vertexRefs) {
        this.id = Objects.requireNonNull(id);
        this.vertexRefs = Objects.requireNonNull(vertexRefs);
    }

    public String getId() {
        return id;
    }

    public List<VertexRef> getVertexRefs() {
        return ImmutableList.copyOf(vertexRefs);
    }

    public List<String> getVertexIds() {
        return vertexRefs.stream().map(v -> v.getId()).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Focus focus = (Focus) o;
        return Objects.equals(id, focus.id)
                && Objects.equals(vertexRefs, focus.vertexRefs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, vertexRefs);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("vertexRefs", vertexRefs)
                .toString();
    }
}
