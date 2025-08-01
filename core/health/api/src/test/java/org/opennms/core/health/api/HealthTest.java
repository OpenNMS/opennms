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
package org.opennms.core.health.api;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.Matchers;
import org.junit.Test;

public class HealthTest {

    @Test
    public void findsWorst() {
        var pairs = Stream.of(Status.values()).map(s -> Pair.<HealthCheck, Response>of(null, new Response(s))).collect(Collectors.toList());
        var h1 = new Health(pairs).getWorst();
        assertThat(h1.get().getRight().getStatus(), Matchers.is(Status.values()[Status.values().length - 1]));
        Collections.reverse(pairs);
        var h2 = new Health(pairs).getWorst();
        assertThat(h2.get().getRight().getStatus(), Matchers.is(Status.values()[Status.values().length - 1]));
    }
}
