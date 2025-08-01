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
package org.opennms.netmgt.flows.classification.persistence.api;

import java.util.Objects;

import com.google.common.base.Preconditions;

public class Protocol {
    private int decimal;
    private String keyword;
    private String description;

    public Protocol(int decimal, String keyword, String description) {
        Objects.requireNonNull(keyword);
        Preconditions.checkArgument(decimal >= 0, "decimal must be >= 0");
        this.decimal = decimal;
        this.keyword = keyword;
        this.description = description;
    }

    public int getDecimal() {
        return decimal;
    }

    public void setDecimal(int decimal) {
        this.decimal = decimal;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Protocol protocol = (Protocol) o;
        return Objects.equals(decimal, protocol.decimal)
                && Objects.equals(keyword, protocol.keyword)
                && Objects.equals(description, protocol.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(decimal, keyword, description);
    }

    @Override
    public String toString() {
        return "Protocol{" +
               "decimal=" + decimal +
               ", keyword='" + keyword + '\'' +
               '}';
    }
}
