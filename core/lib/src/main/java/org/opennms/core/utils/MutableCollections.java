/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
