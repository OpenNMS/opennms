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

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Creates Pairs from the given list where each element is linked to the next one in the list. The end of the list is
 * connected to the beginning.
 */
public class LinkedPairGenerator<E> implements PairGenerator<E> {

    private final List<E> elements;
    private final int lastIndexInList;
    private int indexLeft = -1;
    private int indexRight = 0;

    @Override
    public Pair<E, E> next() {
        compute();
        return Pair.of(elements.get(indexLeft), elements.get(indexRight));
    }

    public LinkedPairGenerator(List<E> elements) {
        if (elements == null || elements.size() < 2) {
            throw new IllegalArgumentException("Need at least 2 elements in list to make a pair");
        }
        this.elements = elements;
        lastIndexInList = elements.size() - 1;

    }

    private void compute() {
        indexLeft = next(indexLeft);
        indexRight = next(indexRight);
    }

    private int next(int i) {
        if (i == lastIndexInList) {
            return 0;
        }
        return ++i;
    }

}
