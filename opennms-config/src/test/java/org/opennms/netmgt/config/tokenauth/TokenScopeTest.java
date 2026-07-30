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
package org.opennms.netmgt.config.tokenauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.opennms.core.mate.api.ContextKey;
import org.opennms.core.mate.api.Scope;

public class TokenScopeTest {

    private final AtomicInteger calls = new AtomicInteger();

    private final TokenProvider provider = authName -> {
        calls.incrementAndGet();
        return "m365".equals(authName) ? Optional.of("the-token") : Optional.empty();
    };

    @Test
    public void resolvesKnownTokenInTokenContext() {
        final TokenScope scope = new TokenScope(provider);
        final Optional<Scope.ScopeValue> value = scope.get(new ContextKey("token", "m365"));
        assertTrue(value.isPresent());
        assertEquals("the-token", value.get().value);
        assertEquals(Scope.ScopeName.GLOBAL, value.get().scopeName);
    }

    @Test
    public void ignoresOtherContexts() {
        final TokenScope scope = new TokenScope(provider);
        assertFalse(scope.get(new ContextKey("scv", "m365")).isPresent());
        assertEquals(0, calls.get());
    }

    @Test
    public void unknownTokenIsEmpty() {
        final TokenScope scope = new TokenScope(provider);
        assertFalse(scope.get(new ContextKey("token", "nope")).isPresent());
    }

    @Test
    public void resolvesOnEveryCall() {
        final TokenScope scope = new TokenScope(provider);
        scope.get(new ContextKey("token", "m365"));
        scope.get(new ContextKey("token", "m365"));
        assertEquals(2, calls.get());
    }

    @Test
    public void exposesNoEnumerableKeys() {
        assertTrue(new TokenScope(provider).keys().isEmpty());
    }
}
