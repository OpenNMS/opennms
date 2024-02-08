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
package org.opennms.netmgt.measurements.model;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "query-request")
@XmlAccessorType(XmlAccessType.NONE)
public class QueryNode implements Comparable<QueryNode> {
    @XmlAttribute private final Integer id;
    @XmlAttribute(name="foreign-source") private final String foreignSource;
    @XmlAttribute(name="foreign-id") private final String foreignId;
    @XmlAttribute private final String label;

    public QueryNode() {
        this.id = null;
        this.foreignSource = null;
        this.foreignId = null;
        this.label = null;
    }

    public QueryNode(final Integer id, final String foreignSource, final String foreignId, final String label) {
        this.id = id;
        this.foreignSource = foreignSource;
        this.foreignId = foreignId;
        this.label = label;
    }

    public Integer getId() {
        return id;
    }
    public String getForeignSource() {
        return foreignSource;
    }
    public String getForeignId() {
        return foreignId;
    }
    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QueryNode other = (QueryNode) obj;

        return Objects.equals(this.id, other.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }
    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("id", this.id)
                .add("foreignSource", this.foreignSource)
                .add("foreignId", this.foreignId)
                .add("label", this.label)
                .toString();
    }

    @Override
    public int compareTo(final QueryNode other) {
        return this.getId() - other.getId();
    }
}
