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
package org.opennms.web.utils;

public class QueryParameters {

    private static final Integer DEFAULT_LIMIT = 10;

    public static class Order {
        private final String column;
        private final boolean desc;

        public Order(String column, boolean desc) {
            this.column = column;
            this.desc = desc;
        }

        public String getColumn() {
            return column;
        }

        public boolean isDesc() {
            return desc;
        }

        public boolean isAsc() {
            return !isDesc();
        }
    }

    private Order order;
    private Integer limit;
    private Integer offset;

    public void setOrder(Order order) {
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getLimit() {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        return limit;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getOffset() {
        if (offset == null) {
            return 0;
        }
        return offset;
    }

    public Page getPage() {
        return new Page(offset, limit);
    }

}
