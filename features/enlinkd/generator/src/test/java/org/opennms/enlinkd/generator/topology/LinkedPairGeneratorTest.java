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
