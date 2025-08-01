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
package org.opennms.install;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockStatic;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.opennms.bootstrap.FilesystemPermissionValidator;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.test.DaoTestConfigBean;
import org.springframework.util.FileSystemUtils;

public class InstallerTest {
    private Path opennmsHome;
    private Path etcDir;

    private Installer installer;

    @Before
    public void setUp() throws Exception {
        opennmsHome = Paths.get("target/test-classes/opennmsconf");
        etcDir = opennmsHome.resolve("etc");
        FileSystemUtils.deleteRecursively(opennmsHome.toFile());

        for (var dir : Arrays.asList("etc", "data", "deploy", "instances", "lib", "logs", "share", "system")) {
            Files.createDirectories(opennmsHome.resolve(dir));
        }

        Files.writeString(etcDir.resolve("opennms.properties"), "");
        Files.writeString(etcDir.resolve("rrd-configuration.properties"), "");

        System.setProperty("skip-native", "true");
        System.setProperty("install.dir", opennmsHome.toString());
        System.setProperty("install.etc.dir", opennmsHome.resolve("etc").toString());

        final DaoTestConfigBean bean = new DaoTestConfigBean();
        bean.setRelativeHomeDirectory(opennmsHome.toString());
        bean.afterPropertiesSet();

        installer = new Installer();

        // FilesystemPermissionValidator.VERBOSE = true;
    }

    @After
    public void tearDown() throws Exception {
        FileSystemUtils.deleteRecursively(opennmsHome.toFile());
        FilesystemPermissionValidator.VERBOSE = false;
    }

    @Test
    public void testReadMissingOpennmsConf() throws Exception {
        final Properties props = installer.readOpennmsConf();
        assertEquals(0, props.size());
    }

    @Test
    public void testReadEmptyOpennmsConf() throws Exception {
        Files.writeString(etcDir.resolve("opennms.conf"), "");
        final Properties props = installer.readOpennmsConf();
        assertEquals(0, props.size());
    }

    @Test
    public void testReadOpennmsConfWithRunas() throws Exception {
        Files.writeString(etcDir.resolve("opennms.conf"), "RUNAS=riptaylor");
        final Properties props = installer.readOpennmsConf();
        assertEquals(1, props.size());
        assertEquals("riptaylor", props.get("RUNAS"));
    }

    @Test
    public void testRunasMissingOpennmsConf() throws Exception {
        final String runas = installer.getRunas();
        assertEquals("opennms", runas);
    }

    @Test
    public void testRunasEmptyOpennmsConf() throws Exception {
        Files.writeString(etcDir.resolve("opennms.conf"), "");
        final String runas = installer.getRunas();
        assertEquals("opennms", runas);
    }

    @Test
    public void testRunasIrrelevantOpennmsConf() throws Exception {
        Files.writeString(etcDir.resolve("opennms.conf"), "FOO=bar");
        final String runas = installer.getRunas();
        assertEquals("opennms", runas);
    }

    @Test
    public void testRunasOverrideProperty() throws Exception {
        System.setProperty("opennms.runas", "riptorn");
        final String runas = installer.getRunas();
        assertEquals("riptorn", runas);
    }

    @Test
    public void testRunasOverrideOpennmsConf() throws Exception {
        Files.writeString(etcDir.resolve("opennms.conf"), "RUNAS=rivjaylen");
        final String runas = installer.getRunas();
        assertEquals("rivjaylen", runas);
    }

    @Test
    public void testRunasOverridePropertyAndOpennmsConf() throws Exception {
        System.setProperty("opennms.runas", "riptorn");
        Files.writeString(etcDir.resolve("opennms.conf"), "RUNAS=rivjaylen");
        final String runas = installer.getRunas();
        assertEquals("rivjaylen", runas);
    }

    @Test
    public void testRunasGitDirectory() throws Exception {
        final var lolDir = Files.createDirectories(etcDir.resolve(".git").resolve("objects"));
        final var lol = lolDir.resolve("42042042042042042042042042042042031337");
        Files.writeString(lol, "");
        Files.setPosixFilePermissions(lol, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.GROUP_READ, PosixFilePermission.OTHERS_READ));

        installer.verifyFilesAndDirectories();
    }

    @Test
    public void testRunasLostAndFoundDirectory() throws Exception {
        final var lostFoundDir = Files.createDirectories(opennmsHome.resolve("share").resolve("lost+found"));
        Files.setPosixFilePermissions(lostFoundDir, Set.of());

        installer.verifyFilesAndDirectories();
    }

    @Test
    public void testPasswordStaysTheSame() throws Exception {
        Files.copy(Paths.get("src/test/resources/etc").resolve("users.xml"), etcDir.resolve("users.xml"));
        Files.copy(Paths.get("src/test/resources/etc").resolve("groups.xml"), etcDir.resolve("groups.xml"));

        UserFactory.init();
        final UserManager userManager = UserFactory.getInstance();
        userManager.reload();

        assertTrue(userManager.comparePasswords("admin", "admin"));
        try {
            installer.install(new String[]{"-s"});
        } catch (Exception e) {
            e.printStackTrace();
        }
        userManager.reload();
        assertTrue(userManager.comparePasswords("admin", "admin"));
    }

    @Test
    public void testPasswordIsSet() throws Exception {
        Files.copy(Paths.get("src/test/resources/etc").resolve("users.xml"), etcDir.resolve("users.xml"));
        Files.copy(Paths.get("src/test/resources/etc").resolve("groups.xml"), etcDir.resolve("groups.xml"));

        UserFactory.init();
        final UserManager userManager = UserFactory.getInstance();
        userManager.reload();

        assertTrue(userManager.comparePasswords("admin", "admin"));
        try {
            installer.install(new String[]{"-R", "foobar"});
        } catch (Exception e) {
            e.printStackTrace();
        }
        userManager.reload();
        assertTrue(userManager.comparePasswords("admin", "foobar"));
    }

    public void testPath(final String path, final String extension) throws Exception {
        try (final MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            // mock Files.exists(), so the path (e.g. /usr/lib64) exists for this test
            filesMock.when(() -> Files.exists(Paths.get(path))).thenReturn(true);
            final Installer installer = new Installer() {
                @Override
                public boolean loadLibrary(String p) {
                    // library exists in the given location
                    return p.equals(path + "/libjrrd2." + extension);
                }
            };
            final String lib = installer.findLibrary("jrrd2", null, true);
            // check that library was found in the given location
            assertEquals(path + "/libjrrd2." + extension, lib);
            System.out.println(lib);
        }
    }

    @Test
    public void testNMS17883() throws Exception {
        // we need this, otherwise the test will either fail on Mac OS X or Linux
        final String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        final String libExtension;
        if (os.contains("mac") || os.contains("darwin")) {
            libExtension = "dylib";
        } else {
            libExtension = "so";
        }
        // check deb- and rpm-based library locations
        testPath("/usr/lib64", libExtension);
        testPath("/usr/lib/jni", libExtension);
    }
}
