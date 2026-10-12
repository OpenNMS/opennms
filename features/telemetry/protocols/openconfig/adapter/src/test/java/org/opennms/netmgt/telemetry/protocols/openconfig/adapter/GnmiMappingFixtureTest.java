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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opennms.features.openconfig.proto.gnmi.Gnmi;

import com.google.protobuf.TextFormat;

/**
 * Golden fixtures for the declarative gNMI mapping. Phase A runs the same directory against the Java engine.
 */
@RunWith(Parameterized.class)
public class GnmiMappingFixtureTest {

    private static final Path CASES = Paths.get("src/test/resources/gnmi-mapping");

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> cases() throws IOException {
        try (Stream<Path> dirs = Files.list(CASES)) {
            return dirs.filter(Files::isDirectory)
                    .map(dir -> dir.getFileName().toString())
                    .sorted()
                    .map(name -> new Object[] {name})
                    .collect(Collectors.toList());
        }
    }

    @Parameterized.Parameter
    public String name;

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private String previousHome;

    @Before
    public void saveHome() {
        previousHome = System.getProperty("opennms.home");
    }

    @After
    public void restoreHome() {
        if (previousHome == null) {
            System.clearProperty("opennms.home");
        } else {
            System.setProperty("opennms.home", previousHome);
        }
    }

    @Test
    public void producesExpectedCollectionSet() throws Exception {
        Path dir = CASES.resolve(name);
        GnmiMappingScript.installMapping(tmp.getRoot(), dir);
        System.setProperty("opennms.home", tmp.getRoot().getAbsolutePath());

        Gnmi.SubscribeResponse.Builder response = Gnmi.SubscribeResponse.newBuilder();
        TextFormat.merge(Files.readString(dir.resolve("input.textproto")), response);

        String actual = GnmiMappingScript.render(GnmiMappingScript.run(response.build()));
        assertEquals(Files.readString(dir.resolve("expected.txt")).trim(), actual);
    }
}
