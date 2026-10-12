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
package org.opennms.netmgt.telemetry.protocols.openconfig.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.nio.file.Files;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.opennms.features.openconfig.proto.gnmi.Gnmi;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;

import groovy.lang.GroovyClassLoader;

public class GnmiMappingScriptTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private String previousHome;

    @Before
    public void pointHomeAtTempDir() {
        previousHome = System.getProperty("opennms.home");
        System.setProperty("opennms.home", tmp.getRoot().getAbsolutePath());
    }

    @After
    public void restoreHome() {
        if (previousHome == null) {
            System.clearProperty("opennms.home");
        } else {
            System.setProperty("opennms.home", previousHome);
        }
    }

    private static Gnmi.SubscribeResponse oneCounter() {
        return Gnmi.SubscribeResponse.newBuilder()
                .setUpdate(Gnmi.Notification.newBuilder()
                        .addUpdate(Gnmi.Update.newBuilder()
                                .setPath(Gnmi.Path.newBuilder()
                                        .addElem(Gnmi.PathElem.newBuilder().setName("system"))
                                        .addElem(Gnmi.PathElem.newBuilder().setName("uptime")))
                                .setVal(Gnmi.TypedValue.newBuilder().setUintVal(3))))
                .build();
    }

    @Test
    public void scriptHasNoMavenFilterTokens() throws Exception {
        String text = Files.readString(GnmiMappingScript.file().toPath());
        assertFalse("the script must not contain a dollar sign", text.contains("$"));
        assertFalse("the script must not contain an @word@ token", Pattern.compile("@[A-Za-z0-9._-]+@").matcher(text).find());
    }

    @Test
    public void noMappingEmitsNothing() throws Exception {
        assertEquals("", GnmiMappingScript.render(GnmiMappingScript.run(oneCounter())));
    }

    @Test
    public void nullAgentEmitsNothing() throws Exception {
        GnmiMappingScript.writeFile(new File(tmp.getRoot(), "etc/telemetryd-adapters"), "openconfig-gnmi-mapping.yaml",
                "version: 1",
                "groups:",
                "  - name: sys",
                "    match: /system",
                "    resource-type: node",
                "    attributes: { uptime: { type: gauge } }");
        GroovyClassLoader gcl = GnmiMappingScript.load();
        CollectionSetBuilder builder = new CollectionSetBuilder(Mockito.mock(CollectionAgent.class));
        GnmiMappingScript.call(gcl, "GnmiMapping", "apply", null, builder, oneCounter());
        assertEquals(0, builder.getNumAttributes());
    }
}
