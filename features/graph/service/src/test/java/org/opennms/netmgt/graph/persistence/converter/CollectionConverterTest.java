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
package org.opennms.netmgt.graph.persistence.converter;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

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
        shouldConvertACollectionAndBack(createCollection(ImmutableList::copyOf, Arrays.asList("A", "B", "C")));
        shouldConvertACollectionAndBack(createCollection(ImmutableSet::copyOf, Arrays.asList("A", "B", "C")));
    }

    @Test
    public void shouldConvertCollectionsOfCollection() {
        shouldConvertACollectionAndBack(createNestedCollection(ImmutableList::copyOf));
        shouldConvertACollectionAndBack(createNestedCollection(ImmutableSet::copyOf));
    }

    private Collection createNestedCollection(Function<Collection, Collection> constructor) {
        Collection<String> data1 = createCollection(constructor, Arrays.asList("A", "B"));
        Collection<String> data2 = createCollection(constructor, Arrays.asList("C", "D"));
        return createCollection(constructor, Arrays.asList(data1, data2));
    }

    private Collection createCollection(Function<Collection, Collection> constructor, Collection data) {
        Collection collection = constructor.apply(data);
        return collection;
    }

    private void shouldConvertACollectionAndBack(Collection originalCollection) {
        CollectionConverter converter = new CollectionConverter(new ConverterService());
        String listAsString = converter.toStringRepresentation(originalCollection);
        Collection convertedBackList = converter.toValue(null, listAsString);
        assertEquals(originalCollection, convertedBackList);
    }
}
