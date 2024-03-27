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
package org.opennms.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Convenience Methods for handling mutable Collections.
 *
 * @author mbrooks
 */
public class MutableCollections {
    /**
     * Copy a Collection to a List.
     *
     * @param toCopy       the Collection to copy
     * @param listSupplier the supplier for the desired List type
     * @return a new List containing the same elements as the given Collection
     */
    public static <T> List<T> copyListFromNullable(Collection<T> toCopy, Supplier<List<T>> listSupplier) {
        return Optional.ofNullable(toCopy)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .collect(Collectors.toCollection(listSupplier));
    }

    public static <T> List<T> copyListFromNullable(Collection<T> toCopy) {
        return copyListFromNullable(toCopy, ArrayList::new);
    }

    /**
     * Copy a Map.
     *
     * @param toCopy      the Map to copy
     * @param mapSupplier the supplier for the desired Map type
     * @return a new Map containing the same elements as the given Map
     */
    public static <T, S> Map<T, S> copyMapFromNullable(Map<T, S> toCopy, Supplier<Map<T, S>> mapSupplier) {
        Map<T, S> map = mapSupplier.get();
        if (toCopy == null || toCopy.isEmpty()) {
            return map;
        }
        map.putAll(toCopy);
        return map;
    }

    public static <T, S> Map<T, S> copyMapFromNullable(Map<T, S> toCopy) {
        return copyMapFromNullable(toCopy, HashMap::new);
    }
}
