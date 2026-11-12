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
package org.opennms.netmgt.config.threshd;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

@XmlJavaTypeAdapter(FilterOperatorAdapter.class)
@XmlRootElement(name="filterOperator")
@XmlEnum
public enum FilterOperator {
    @XmlEnumValue("and")
    AND,
    @XmlEnumValue("or")
    OR;

    public String getEnumName() {
        return this.toString().toLowerCase();
    }

    /**
     * Configuration manager JSON uses the same representation as {@link FilterOperatorAdapter}: written as the
     * constant name, read case-insensitively (configurations imported from XML hold "and"/"or").
     */
    @JsonValue
    String toJson() {
        return name();
    }

    @JsonCreator
    static FilterOperator fromJson(final String value) {
        return value == null ? null : FilterOperator.valueOf(value.toUpperCase());
    }
}
