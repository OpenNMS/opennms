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

import java.util.Collections;
import java.util.List;

public class Page {
    private Integer offset;
    private Integer limit;

    public Page(Integer offset, Integer limit) {
        if (offset != null && offset.intValue() < 0) {
            throw new IllegalArgumentException("Offset must be > 0");
        }
        if (limit != null && limit <= 0) {
            throw new IllegalArgumentException("limit must be > 0");
        }
        this.offset = offset;
        this.limit = limit;
    }

    public <T> List<T> apply(List<T> list) {
        if (offset != null && offset > list.size()) {
            return Collections.emptyList();
        }
        if (offset != null && limit != null) {
            return sublist(list, offset, limit);
        }
        if (offset != null) {
            return list.subList(offset, list.size());
        }
        if (limit != null) {
            sublist(list, 0, limit);
        }
        return list;
    }

    private static <T> List<T> sublist(List<T> list, int start, int limit) {
        if (start + limit > list.size()) {
            limit = list.size() - start;
        }
        return list.subList(start, start + limit);
    }

}
