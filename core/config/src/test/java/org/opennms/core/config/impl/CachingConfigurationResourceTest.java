package org.opennms.core.config.impl;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.opennms.core.config.api.CachingConfigurationResource;
import org.opennms.core.config.api.ConfigurationResourceException;

public class CachingConfigurationResourceTest {
    @Test
    public void testGet() throws Exception {
        final AtomicReference<String> ret = new AtomicReference<>("blah");
        final CachingConfigurationResource<String> resource = new AbstractCachingConfigurationResource<String>() {
            @Override protected String load() throws ConfigurationResourceException {
                return ret.get();
            }
            @Override protected void save(final String config) throws ConfigurationResourceException {
            }
        };

        assertEquals("blah", resource.get());
        ret.set("foo");
        assertEquals("blah", resource.get());
        resource.invalidate();
        assertEquals("foo", resource.get());
    }

    @Test
    public void testPut() throws Exception {
        final AtomicReference<String> ret = new AtomicReference<>("blah");
        final CachingConfigurationResource<String> resource = new AbstractCachingConfigurationResource<String>() {
            @Override protected String load() throws ConfigurationResourceException {
                return ret.get();
            }
            @Override protected void save(final String config) throws ConfigurationResourceException {
                ret.set(config);
            }
        };

        assertEquals("blah", resource.get());
        resource.put("foo");
        assertEquals("foo", resource.get());
        assertEquals("foo", ret.get());
    }
}
