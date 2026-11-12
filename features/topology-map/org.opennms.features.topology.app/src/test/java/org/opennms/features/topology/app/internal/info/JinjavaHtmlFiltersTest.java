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
package org.opennms.features.topology.app.internal.info;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;
import java.util.jar.JarFile;

import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.RenderResult;

/**
 * Jinjava's striptags and truncatehtml filters are backed by jsoup, which
 * org.opennms.dependencies.jinjava-shaded replaces with the project's jsoup version.
 * These tests catch jsoup updates that break the filters used in the info panel.
 */
public class JinjavaHtmlFiltersTest {

    private final Jinjava jinjava = new Jinjava();

    @Test
    public void testJinjavaUsesProjectJsoup() throws Exception {
        final File jinjavaJar = new File(Jinjava.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        final Properties jsoup = new Properties();
        try (JarFile jar = new JarFile(jinjavaJar);
             InputStream in = jar.getInputStream(jar.getEntry("META-INF/maven/org.jsoup/jsoup/pom.properties"))) {
            jsoup.load(in);
        }
        assertEquals(System.getProperty("jsoupVersion"), jsoup.getProperty("version"));
    }

    @Test
    public void testStriptags() {
        assertEquals("Hello World", render("{{ \"<p>Hello <b>World</b></p>\"|striptags }}"));
    }

    @Test
    public void testTruncatehtml() {
        assertEquals("<p>Hello <b>Wo...</b></p>", render("{{ \"<p>Hello <b>World</b> foo bar</p>\"|truncatehtml(8, \"...\", true) }}"));
    }

    private String render(final String template) {
        final RenderResult result = jinjava.renderForResult(template, Collections.emptyMap());
        assertTrue(result.getErrors().toString(), result.getErrors().isEmpty());
        return result.getOutput().trim();
    }
}
