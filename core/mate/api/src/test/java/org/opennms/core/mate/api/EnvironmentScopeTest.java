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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.Set;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class EnvironmentScopeTest {
    private EnvironmentScope envScope;

    @Before
    public void setUp() {
        envScope = new EnvironmentScope();
    }

    @Test
    public void testGetExistingEnvironmentVariable() {
        // PATH should exist on all systems
        String pathValue = System.getenv("PATH");
        Assume.assumeNotNull("PATH environment variable must be set", pathValue);

        Optional<Scope.ScopeValue> result = envScope.get(new ContextKey("env", "PATH"));
        assertTrue("Should find PATH environment variable", result.isPresent());
        assertEquals("Should return correct PATH value", pathValue, result.get().value);
        assertEquals("Should use GLOBAL scope", Scope.ScopeName.GLOBAL, result.get().scopeName);
    }

    @Test
    public void testGetNonExistentEnvironmentVariable() {
        Optional<Scope.ScopeValue> result = envScope.get(new ContextKey("env", "NONEXISTENT_VAR_12345"));
        assertFalse("Should not find non-existent environment variable", result.isPresent());
    }

    @Test
    public void testGetWithWrongContext() {
        Optional<Scope.ScopeValue> result = envScope.get(new ContextKey("wrong", "PATH"));
        assertFalse("Should not match wrong context", result.isPresent());
    }

    @Test
    public void testGetWithEmptyKey() {
        Optional<Scope.ScopeValue> result = envScope.get(new ContextKey("env", ""));
        assertFalse("Should not match empty key", result.isPresent());
    }

    @Test(expected = NullPointerException.class)
    public void testGetWithNullKey() {
        // ContextKey constructor will throw NullPointerException for null key
        envScope.get(new ContextKey("env", null));
    }

    @Test
    public void testKeys() {
        Set<ContextKey> keys = envScope.keys();
        assertFalse("Should have at least some environment variables", keys.isEmpty());

        // Verify all keys have the correct context
        for (ContextKey key : keys) {
            assertEquals("All keys should have 'env' context", "env", key.context);
        }
    }

    @Test
    public void testInterpolationWithExistingVariable() {
        // Use PATH which should exist on all systems
        String pathValue = System.getenv("PATH");
        Assume.assumeNotNull("PATH environment variable must be set", pathValue);

        Interpolator.Result result = Interpolator.interpolate(
                "The path is: ${env:PATH}",
                envScope
        );
        assertEquals("Should interpolate PATH variable", "The path is: " + pathValue, result.output);
    }

    @Test
    public void testInterpolationWithNonExistentVariableAndDefault() {
        Interpolator.Result result = Interpolator.interpolate(
                "Value: ${env:NONEXISTENT_VAR_12345|default_value}",
                envScope
        );
        assertEquals("Should use default value", "Value: default_value", result.output);
    }

    @Test
    public void testInterpolationWithNonExistentVariableNoDefault() {
        Interpolator.Result result = Interpolator.interpolate(
                "Value: ${env:NONEXISTENT_VAR_12345}",
                envScope
        );
        assertEquals("Should result in empty string", "Value: ", result.output);
    }

    @Test
    public void testMultipleInterpolations() {
        String pathValue = System.getenv("PATH");
        Assume.assumeNotNull("PATH environment variable must be set", pathValue);

        Interpolator.Result result = Interpolator.interpolate(
                "Path: ${env:PATH}, Missing: ${env:MISSING_VAR|fallback}",
                envScope
        );
        assertEquals("Should interpolate both variables",
                "Path: " + pathValue + ", Missing: fallback",
                result.output);
    }
}
