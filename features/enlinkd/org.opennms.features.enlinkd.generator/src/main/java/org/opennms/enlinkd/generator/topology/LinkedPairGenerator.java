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
