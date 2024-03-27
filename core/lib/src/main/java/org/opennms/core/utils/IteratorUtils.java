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
