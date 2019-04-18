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

package org.opennms.netmgt.graph.persistence.converter;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Supplier;

import org.junit.Test;

public class CollectionConverterTest {

    @Test
    public void shouldConvertACollectionAndBack() {
        List<String> originalList = new ArrayList<>();
        originalList.addAll(Arrays.asList("A", "B", "C"));
        shouldConvertACollectionAndBack(originalList);
    }

    @Test
    public void shouldConvertAnEmptyCollectionAndBack() {
        shouldConvertACollectionAndBack(Collections.EMPTY_LIST);
    }

    @Test
    public void shouldConvertDifferentCollections() {
        shouldConvertACollectionAndBack(createCollection(ArrayList::new, "A", "B", "C"));
        shouldConvertACollectionAndBack(createCollection(HashSet::new, "A", "B", "C"));
        shouldConvertACollectionAndBack(createCollection(TreeSet::new, "A", "B", "C"));
        shouldConvertACollectionAndBack(createCollection(LinkedList::new, "A", "B", "C"));
    }

    @Test
    public void shouldConvertCollectionsOfCollection() {
        shouldConvertACollectionAndBack(createNestedCollection(ArrayList::new));
        shouldConvertACollectionAndBack(createNestedCollection(HashSet::new));
        shouldConvertACollectionAndBack(createNestedCollection(LinkedList::new));
    }

    private Collection<Collection<String>> createNestedCollection(Supplier<Collection> constructor) {
        Collection<String> data1 = createCollection(constructor, "A", "B");
        Collection<String> data2 = createCollection(constructor, "C", "D");
        return createCollection(constructor, data1, data2);
    }

    private <T> Collection<T> createCollection(Supplier<Collection> constructor, T...data) {
        Collection<T> collection = constructor.get();
        Collections.addAll(collection, data);
        return collection;
    }

    private void shouldConvertACollectionAndBack(Collection originalCollection) {
        CollectionConverter converter = new CollectionConverter(new ConverterService());
        String listAsString = converter.toStringRepresentation(originalCollection);
        Collection convertedBackList = converter.toValue(null, listAsString);
        assertEquals(originalCollection, convertedBackList);
    }
}
