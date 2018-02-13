/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.assets.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.web.assets.api.AssetResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

public class AssetLocatorImplTest {
    private AssetLocatorImpl m_locator;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        m_locator = new AssetLocatorImpl();
        m_locator.afterPropertiesSet();
    }

    @Test
    public void testGetAssets() throws Exception {
        assertNotNull(m_locator.getAssets());
        assertEquals(1, m_locator.getAssets().size());
        assertEquals("test-asset", m_locator.getAssets().iterator().next());
    }

    @Test
    public void testGetAssetsUnminified() throws Exception {
        assertNotNull(m_locator.getAssets(false));
        assertEquals(1, m_locator.getAssets(false).size());
        assertEquals("test-asset", m_locator.getAssets(false).iterator().next());
    }

    @Test
    public void testGetResources() throws Exception {
        final Optional<Collection<AssetResource>> resources = m_locator.getResources("test-asset");
        assertTrue(resources.isPresent());
        assertNotNull(resources.get());
        assertEquals(1, resources.get().size());
        final AssetResource resource = resources.get().iterator().next();
        assertEquals("test-asset", resource.getAsset());
        assertEquals("js", resource.getType());
        assertEquals("assets/test.min.js", resource.getPath());
    }

    @Test
    public void testGetResourcesUnminified() throws Exception {
        final Optional<Collection<AssetResource>> resources = m_locator.getResources("test-asset", false);
        assertTrue(resources.isPresent());
        assertNotNull(resources.get());
        assertEquals(1, resources.get().size());
        final AssetResource resource = resources.get().iterator().next();
        assertEquals("test-asset", resource.getAsset());
        assertEquals("js", resource.getType());
        assertEquals("assets/test.js", resource.getPath());
    }

    @Test
    public void testMissingResources() throws Exception {
        final Optional<Collection<AssetResource>> resources = m_locator.getResources("missing");
        assertFalse(resources.isPresent());
        
        final Optional<AssetResource> resource = m_locator.getResource("missing", "js");
        assertFalse(resource.isPresent());
    }

    @Test
    public void testMissingResourcesUnminified() throws Exception {
        final Optional<Collection<AssetResource>> resources = m_locator.getResources("missing", false);
        assertFalse(resources.isPresent());
        
        final Optional<AssetResource> resource = m_locator.getResource("missing", "js");
        assertFalse(resource.isPresent());
    }

    @Test
    public void testReadResource() throws Exception {
        final Optional<InputStream> is = m_locator.open("test-asset", "js");
        assertTrue(is.isPresent());
        final InputStreamReader isr = new InputStreamReader(is.get());
        final String contents = FileCopyUtils.copyToString(isr);
        assertTrue(contents.contains("yo"));
        assertTrue(contents.contains("console.log"));
    }

    @Test
    public void testReadResourceUnminified() throws Exception {
        final Optional<InputStream> is = m_locator.open("test-asset", "js", false);
        assertTrue(is.isPresent());
        final InputStreamReader isr = new InputStreamReader(is.get());
        final String contents = FileCopyUtils.copyToString(isr);
        assertTrue(contents.contains("yo"));
        assertTrue(contents.contains("console.log"));
    }

    @Test
    public void testResolveFromClasspath() throws Exception {
        final Resource location = new ClassPathResource("/assets/", AssetLocatorImpl.class);
        final String requestPath = "test-asset.js";
        final Resource actual = m_locator.resolveResource(null, requestPath, Arrays.asList(location), null);
        assertNotNull(actual);
        final URL expected = location.createRelative("test.min.js").getURL();
        final URL found = actual.getURL();
        assertEquals(expected, found);
   }

    @Test
    public void testResolveFromFileystem() throws Exception {
        final Resource location = new ClassPathResource("/assets/", AssetLocatorImpl.class);
        final String requestPath = "test.js";
        final Resource actual = m_locator.resolveResource(null, requestPath, Arrays.asList(location), null);
        assertNotNull(actual);
        final URL expected = location.createRelative("test.js").getURL();
        final URL found = actual.getURL();
        assertEquals(expected, found);
   }
}
