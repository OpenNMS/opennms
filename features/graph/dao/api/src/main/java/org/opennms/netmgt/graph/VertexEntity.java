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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.opennms.netmgt.graph.dao.api.EntityProperties;

@Entity
@DiscriminatorValue("vertex")
public class VertexEntity extends AbstractGraphEntity {

    /** returns the business id of the VertexEntity. For the database id call VertexEntity#getDbId. */
    public String getId() {
        return this.getPropertyValue(EntityProperties.ID);
    }

    /** returns the business id of the VertexEntity. For the database id call VertexEntity#getDbId. */
    public void setId(String id) {
        Objects.requireNonNull(id);
        this.setProperty(EntityProperties.ID, String.class, id);
    }

}
