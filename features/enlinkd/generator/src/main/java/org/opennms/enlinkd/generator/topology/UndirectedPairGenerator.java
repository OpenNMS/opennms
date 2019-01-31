/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.enlinkd.generator.topology;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Takes a list and generates pairs among its elements. It tries to distribute the pairs equally as in as least same
 * pairs as possible. Pair(a,b) counts as equal to Pair(b,a).
 */
public class UndirectedPairGenerator<E> implements PairGenerator<E> {

    private final List<E> elements;
    private final int lastIndexInList;
    private int indexLeft = 0;
    private int indexRight = 0;

    @Override
    public Pair<E, E> next() {
        computeIndexForUnique();
        return Pair.of(elements.get(indexLeft), elements.get(indexRight));
    }

    public UndirectedPairGenerator(List<E> elements) {
        if (elements == null || elements.size() < 2) {
            throw new IllegalArgumentException("Need at least 2 elements in list to make a pair");
        }
        this.elements = elements;
        lastIndexInList = elements.size() - 1;
    }

    private void computeIndexForUnique() {
        computeIndex();
        while (indexLeft >= indexRight) {
            computeIndex(); // jump over doubles
        }
    }

    private void computeIndex() {
        if (indexLeft == lastIndexInList && this.indexRight == lastIndexInList - 1) {
            // start from beginning
            this.indexLeft = 0;
            this.indexRight = 0;
        } else if (this.indexRight == lastIndexInList) {
            this.indexLeft = next(this.indexLeft);
        }
        this.indexRight = nextButNotSame(this.indexRight, this.indexLeft);
    }

    private int nextButNotSame(int current, int notSame) {
        int value = next(current);
        if (value == notSame) {
            value = next(value);
        }
        return value;
    }

    private int next(int i) {
        if (i == lastIndexInList) {
            return 0;
        }
        return ++i;
    }
}
