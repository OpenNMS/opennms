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
package org.opennms.core.mate.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class LazyScopeTest {

    private static final ContextKey CONTEXT_KEY = new ContextKey("node", "label");

    @Test
    public void delegateIsNotBuiltWithoutLookup() {
        final AtomicInteger builds = new AtomicInteger();
        new LazyScope(() -> {
            builds.incrementAndGet();
            return EmptyScope.EMPTY;
        });

        assertThat(builds.get(), is(0));
    }

    @Test
    public void delegateIsBuiltAtMostOnce() {
        final AtomicInteger builds = new AtomicInteger();
        final Scope scope = new LazyScope(() -> {
            builds.incrementAndGet();
            return MapScope.singleContext(Scope.ScopeName.NODE, "node", Map.of("label", "n1"));
        });

        assertThat(scope.get(CONTEXT_KEY).map(v -> v.value).orElse(null), is("n1"));
        assertThat(scope.get(CONTEXT_KEY).map(v -> v.value).orElse(null), is("n1"));
        assertThat(scope.keys().contains(CONTEXT_KEY), is(true));
        assertThat(builds.get(), is(1));
    }

    @Test
    public void interpolationWithoutExpressionsNeverBuildsDelegate() {
        final AtomicInteger builds = new AtomicInteger();
        final Scope scope = new LazyScope(() -> {
            builds.incrementAndGet();
            return EmptyScope.EMPTY;
        });

        final Interpolator.Result result = Interpolator.interpolate("no expressions here", scope);

        assertThat(result.output, is("no expressions here"));
        assertThat(builds.get(), is(0));
    }

    @Test
    public void interpolationWithExpressionBuildsDelegate() {
        final AtomicInteger builds = new AtomicInteger();
        final Scope scope = new LazyScope(() -> {
            builds.incrementAndGet();
            return MapScope.singleContext(Scope.ScopeName.NODE, "node", Map.of("label", "n1"));
        });

        final Interpolator.Result result = Interpolator.interpolate("${node:label}", scope);

        assertThat(result.output, is("n1"));
        assertThat(builds.get(), is(1));
    }
}
