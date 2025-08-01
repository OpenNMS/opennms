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

import static org.junit.Assert.assertNotSame;
import static org.opennms.enlinkd.generator.Asserts.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class RandomConnectedPairGeneratorTest {

    @Test
    public void shouldRejectListsWithLessThan2Elements() {
        assertThrows(IllegalArgumentException.class, () -> new RandomConnectedPairGenerator<>(null));
        assertThrows(IllegalArgumentException.class, () -> new RandomConnectedPairGenerator<>(Collections.emptyList()));
    }


    @Test
    public void shouldRejectListsWichContainsOnlyTheSameElement() {
        List<String> list = Arrays.asList("same", "same", "same");
        assertThrows(IllegalArgumentException.class, () -> new RandomConnectedPairGenerator<>(list));
    }

    @Test
    public void shouldNotPairElementsWithItself() {
        List<String> list = Arrays.asList("1", "2", "3");
        RandomConnectedPairGenerator generator = new RandomConnectedPairGenerator<>(list);
        for (int i = 0; i < 10; i++) {
            Pair pair = generator.next();
            assertNotSame(pair.getLeft(), pair.getRight());
        }
    }

}
