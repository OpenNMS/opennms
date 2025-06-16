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
package org.opennms.netmgt.search.api;

import java.util.Objects;

/**
 * Represents a search match, allowing some additional feedback to the user.
 *
 * A node for example can be found either by label, category, ip address or asset field.
 * The {@link SearchResultItem} however may represent the same node. For the user however it would be useful which
 * criteria of the node actually matched the search input query. A {@link Match} allows to do that.
 *
 * @author mvrueden
 */
public class Match {
    /** Unique ID */
    private String id;

    /** User friendly label */
    private String label;

    /** The value which actually matched. */
    private String value;

    public Match() {

    }

    public Match(String id, String label, String value) {
        this.id = Objects.requireNonNull(id);
        this.label = Objects.requireNonNull(label);
        this.value = Objects.requireNonNull(value);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("label", label)
                .add("value", value)
                .toString();
    }
}
