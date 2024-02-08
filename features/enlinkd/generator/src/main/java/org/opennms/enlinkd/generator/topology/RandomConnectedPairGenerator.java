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
package org.opennms.enlinkd.generator.topology;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Pairs elements randomly but not the same element to itself.
 */
public class RandomConnectedPairGenerator<E> implements PairGenerator<E> {
    private final List<E> elements;
    private Random random = new Random(42);

    public RandomConnectedPairGenerator(List<E> elements) {
        if (elements == null || elements.size() < 2) {
            throw new IllegalArgumentException("Need at least 2 elements in list to make a pair");
        }
        if (new HashSet<>(elements).size() < elements.size()) {
            throw new IllegalArgumentException("List contains at least one duplicate");
        }
        this.elements = elements;
    }

    @Override
    public Pair<E, E> next() {
        E leftElement = getRandomElement(elements);
        E rightElement = getRandomElementButNotSame(elements, leftElement);
        return Pair.of(leftElement, rightElement);
    }

    private E getRandomElementButNotSame(List<E> elements, E notSame) {
        E value = getRandomElement(elements);
        while (value.equals(notSame)) {
            value = getRandomElement(elements);
        }
        return value;
    }

    private E getRandomElement(List<E> list) {
        return list.get(random.nextInt(list.size()));
    }
}
