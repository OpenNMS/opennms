/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
