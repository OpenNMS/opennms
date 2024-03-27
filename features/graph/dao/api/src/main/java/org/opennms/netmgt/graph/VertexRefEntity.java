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
package org.opennms.netmgt.graph;


import java.util.Objects;

import javax.persistence.Embeddable;


@Embeddable
public class VertexRefEntity {
    private final String namespace;
    private final String id;

    /*
    * Default constructor for hibernate. It is not to be used other than from hibernate
    */
    private VertexRefEntity() {
        // trick compiler:
        this.namespace = null;
        this.id = null;
    }
    
    public VertexRefEntity(String namespace, String id) {
        this.namespace = namespace;
        this.id = id;
    }

    /** Copy constructor */
    public VertexRefEntity(VertexRefEntity genericVertexRef){
        this(genericVertexRef.namespace, genericVertexRef.id);
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VertexRefEntity that = (VertexRefEntity) o;
        return Objects.equals(namespace, that.namespace) &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(namespace, id);
    }

    @Override
    public String toString() {
        return namespace + ":" + id;
    }
}
