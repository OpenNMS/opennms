package org.opennms.core.mate.api;

import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

public class ScopeProviderTest {

    private static class TestScopeProvider implements ScopeProvider {
        private final Map<ContextKey, String> map;
        private int calls = 0;

        public TestScopeProvider(Map<ContextKey, String> map) {
            this.map = map;
        }

        @Override
        public Scope getScope() {
            calls++;
            return new MapScope(Scope.ScopeName.GLOBAL, map);
        }
    }

    @Test
    public void testStringWithMetadata() {
        final String s = "this is the ${context:key}";
        final Map<ContextKey, String> map = new TreeMap<>();
        map.put(new ContextKey("context", "key"), "value");
        final TestScopeProvider scopeProvider = new TestScopeProvider(map);

        Interpolator.interpolate(s, scopeProvider);
        final Interpolator.Result result = Interpolator.interpolate(s, scopeProvider);
        assertEquals("this is the value", result.output);
        assertEquals(1, scopeProvider.calls);
    }


    @Test
    public void testStringWithoutMetadata() {
        final String s = "this is the value";
        final Map<ContextKey, String> map = new TreeMap<>();
        map.put(new ContextKey("context", "key"), "value");
        final TestScopeProvider scopeProvider = new TestScopeProvider(map);

        Interpolator.interpolate(s, scopeProvider);
        final Interpolator.Result result = Interpolator.interpolate(s, scopeProvider);
        assertEquals("this is the value", result.output);
        assertEquals(0, scopeProvider.calls);
    }
}
