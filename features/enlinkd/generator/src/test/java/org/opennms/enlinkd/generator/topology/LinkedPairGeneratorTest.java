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

import static org.junit.Assert.assertEquals;
import static org.opennms.enlinkd.generator.Asserts.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class LinkedPairGeneratorTest {

    @Test
    public void shouldRejectListsWithLessThan2Elements() {
        assertThrows(IllegalArgumentException.class, () -> new LinkedPairGenerator<>(null));
        assertThrows(IllegalArgumentException.class, () -> new LinkedPairGenerator<>(Collections.emptyList()));
    }

    @Test
    public void shouldProduceASequenceOfUniquePairs() {
        List<String> list = Arrays.asList("1", "2", "3", "4", "5");
        LinkedPairGenerator gen = new LinkedPairGenerator<>(list);
        assertEquals(Pair.of("1", "2"), gen.next());
        assertEquals(Pair.of("2", "3"), gen.next());
        assertEquals(Pair.of("3", "4"), gen.next());
        assertEquals(Pair.of("4", "5"), gen.next());
        assertEquals(Pair.of("5", "1"), gen.next());
        // and it starts from the beginning again:
        assertEquals(Pair.of("1", "2"), gen.next());
        assertEquals(Pair.of("2", "3"), gen.next());
    }
}
