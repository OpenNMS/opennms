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
package org.opennms.web.utils.assets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.web.utils.assets.AssetLocatorImpl;
import org.opennms.web.utils.assets.AssetResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
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
        final Resource location = new ClassPathResource("/assets/");
        final String requestPath = "test-asset.js";
        final Resource actual = m_locator.resolveResource(null, requestPath, Arrays.asList(location), null);
        assertNotNull(actual);
        final URL expected = location.createRelative("test.min.js").getURL();
        final URL found = actual.getURL();
        assertEquals(expected, found);
   }

    @Test
    public void testResolveFromFileystem() throws Exception {
        final AssetLocatorImpl locator = new AssetLocatorImpl();
        locator.m_filesystemPath = Paths.get("src", "test", "resources", "assets").toAbsolutePath().toString();
        final String requestPath = "test.js";
        final Resource actual = locator.resolveResource(null, requestPath, Arrays.asList(new ClassPathResource("/assets/")), null);
        assertNotNull(actual);
        assertTrue(actual instanceof FileSystemResource);
        final URL expected = new FileSystemResource(locator.m_filesystemPath + "/").createRelative("test.js").getURL();
        final URL found = actual.getURL();
        assertEquals(expected, found);
   }
}
