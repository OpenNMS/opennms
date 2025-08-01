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
package org.opennms.features.jest.client.index;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class StringCollapserTest {

    @Test
    public void shouldCollapseAllStringsWhenCollapsingAtBeginningIsTrue() {
        List<String> result = StringCollapser
                .forList(Arrays.asList("aa1", "aa2","aa3","aa4","xx1"))
                .collapseAfterChars(2)
                .doCollapsingAtBeginning(true)
                .collapse();
        assertEquals("aa*", result.get(0));
        assertEquals("xx*", result.get(1));
    }

    @Test
    public void shouldCollapseNoStringsAtBeginningIfCollapsingAtBeginningIsFalse() {
        List<String> result = StringCollapser
                .forList(Arrays.asList("aa1", "aa2","aa3","aa4","xx1"))
                .collapseAfterChars(2)
                .doCollapsingAtBeginning(false)
                .collapse();
        assertEquals(5, result.size());
        assertEquals("aa1", result.get(0));
        assertEquals("xx*", result.get(4));

    }

    @Test
    public void shouldCollapseNoStringsAtEndIfCollapsingAtEndIsFalse() {
        List<String> result = StringCollapser
                .forList(Arrays.asList("aa1", "aa2","aa3","aa4","xx1", "xx2"))
                .collapseAfterChars(2)
                .doCollapsingAtBeginning(true)
                .doCollapsingAtEnd(false)
                .collapse();
        assertEquals("aa*", result.get(0));
        assertEquals("xx1", result.get(1));
        assertEquals("xx2", result.get(2));
        assertEquals(3, result.size());
    }
    @Test
    public void shouldCollapseNoStringsAtEitherEnd() {
        List<String> result = StringCollapser
                .forList(Arrays.asList("aa1", "aa2","bb1","bb2","xx1", "xx2"))
                .collapseAfterChars(2)
                .doCollapsingAtBeginning(false)
                .doCollapsingAtEnd(false)
                .collapse();

        assertEquals("aa1", result.get(0));
        assertEquals("aa2", result.get(1));
        assertEquals("bb*", result.get(2));
        assertEquals("xx1", result.get(3));
        assertEquals("xx2", result.get(4));
        assertEquals(5, result.size());
    }
}