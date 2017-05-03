/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.ORDERED;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class IteratorUtils<T> {

    /**
     * Use {@link Stream#concat(Stream, Stream)} to combine the iterators.
     * CAUTION: The Iterable returned by this method is not reusable and does
     * not support {@link Iterator#remove()} because it is backed by a {@link Stream}.
     *
     * @param iterators a {@link java.util.Iterator} object.
     */
    @SafeVarargs
    public static <T> Iterable<T> concatIterators(final Iterator<T>... iterators) {
        return concatIterators(Arrays.asList(iterators));
    }

    /**
     * Use {@link Stream#concat(Stream, Stream)} to combine the iterators.
     * CAUTION: The Iterable returned by this method is not reusable and does
     * not support {@link Iterator#remove()} because it is backed by a {@link Stream}.
     *
     * @param iterators a {@link java.util.Iterator} object.
     */
    public static <T> Iterable<T> concatIterators(final List<Iterator<T>> iterators) {
        Stream<T> stream = Stream.<T>empty();
        for (Iterator<T> iterator : iterators) {
            stream = Stream.concat(
                stream,
                StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(iterator, ORDERED | IMMUTABLE),
                    false
                )
            );
        }
        return stream::iterator;
    }
}
