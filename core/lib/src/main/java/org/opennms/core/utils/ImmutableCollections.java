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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for copying a collection ensuring copied elements are immutable.
 * <p>
 * This class is used to achieve deep immutability for value classes by ensuring collections in those classes
 * contain immutable elements in addition to being immutable themselves.
 *
 * @param <T> the type the returned collection will hold
 *
 * @author mbrooks
 */
public class ImmutableCollections<T> {
    private final Function<T, T> immutableInstanceFactory;

    private ImmutableCollections(Function<T, T> immutableInstanceFactory) {
        this.immutableInstanceFactory = immutableInstanceFactory;
    }

    /**
     * Creates an instance that maps elements to an immutable form using the given mapping function.
     * <p>
     * The mapping function may return the element unmapped if it is already considered immutable.
     *
     * @param immutableInstanceFactory a function for mapping an element to an immutable copy of that element
     * @return a new instance with the given mapping function
     */
    public static <T> ImmutableCollections<T> with(Function<T, T> immutableInstanceFactory) {
        return new ImmutableCollections<>(Objects.requireNonNull(immutableInstanceFactory));
    }

    /**
     * Create a new unmodifiable List containing immutable copies of the given elements.
     *
     * @param toCopy the Collection containing the elements to be copied
     * @return a new unmodifiable List containing immutable copies of the given elements
     */
    public List<T> newList(Collection<T> toCopy) {
        if (toCopy == null || toCopy.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(toStreamOfImmutables(toCopy)
                .collect(Collectors.toList()));
    }

    /**
     * Creates an unmodifiable copy of a Collection that already contains immutable types. This is essentially a
     * convenience method for avoiding NPE when using a List copy constructor.
     *
     * @param toCopy the Collection to copy
     * @return an unmodifiable List containing the same elements as in the given Collection
     */
    public static <T> List<T> newListOfImmutableType(Collection<T> toCopy) {
        if (toCopy == null || toCopy.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(toCopy));
    }

    /**
     * Creates an unmodifiable copy of a Map that already contains immutable types. This is essentially a
     * convenience method for avoiding NPE when using a Map copy constructor.
     *
     * @param toCopy      the collection to copy
     * @param mapSupplier a supplier that supplies the desired type of Map
     * @return an unmodifiable Map containing the same elements as in the given Map
     */
    public static <T, S> Map<T, S> newMapOfImmutableTypes(Map<T, S> toCopy, Supplier<Map<T, S>> mapSupplier) {
        if (toCopy == null || toCopy.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<T, S> map = mapSupplier.get();
        map.putAll(toCopy);
        return Collections.unmodifiableMap(map);
    }

    public static <T, S> Map<T, S> newMapOfImmutableTypes(Map<T, S> toCopy) {
        return newMapOfImmutableTypes(toCopy, HashMap::new);
    }

    private Stream<T> toStreamOfImmutables(Collection<T> toCopy) {
        return toCopy.stream().map(immutableInstanceFactory);
    }
}

