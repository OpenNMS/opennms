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
package org.opennms.core.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.Test;

public class IteratorUtilsTest {

	@Test
	public void testConcatIterators() {
		List<String> list = Arrays.asList(new String[] {
			"one",
			"two",
			"three"
		});

		// These elements will be alphabetized
		Set<String> set = new TreeSet<>();
		set.add("four");
		set.add("five");
		set.add("six");
		set.add("seven");

		Iterable<String> iterable = IteratorUtils.concatIterators(list.iterator(), set.iterator());

		assertEquals(
			"one,two,three,five,four,seven,six",
			StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.joining(","))
		);
	}
}
