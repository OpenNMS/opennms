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
package org.opennms.netmgt.graph.api;

import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

/** immutable composite key that is unique over all graphs / graph containers */
public final class VertexRef {

    private final String namespace;
    private final String id;

    public VertexRef(String namespace, String id) {
        this.namespace = requireNotEmpty(namespace, "namespace");
        this.id = requireNotEmpty(id, "id");
    }

    public String getNamespace(){
        return namespace;
    }

    public String getId(){
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VertexRef vertexRef = (VertexRef) o;
        return Objects.equals(namespace, vertexRef.namespace) &&
                Objects.equals(id, vertexRef.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("namespace", namespace)
                .add("id", id)
                .toString();
    }

    private static String requireNotEmpty(String stringToAssert, String attributeName) {
        if (Strings.isNullOrEmpty(stringToAssert)) {
            throw new IllegalArgumentException(String.format("%s cannot be null or empty", attributeName));
        }
        return stringToAssert;
    }
}
