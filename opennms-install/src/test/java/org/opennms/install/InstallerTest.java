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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import org.opennms.netmgt.config.api.UserMgmtStore;
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
        // install() copies etc/opennms.properties into the system properties
        System.clearProperty(UserMgmtStore.PROPERTY);
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

        try {
            installer.verifyFilesAndDirectories();
        } finally {
            // restore permissions so the directory can be cleaned up: Spring 5's
            // FileSystemUtils.deleteRecursively is NIO-based and silently fails
            // on unreadable directories, leaving them behind for later build steps
            Files.setPosixFilePermissions(lostFoundDir, Set.of(PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE));
        }
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
            // -S: these tests check the password; Upgrade (on the classpath with core.upgrade) would need a full etc
            installer.install(new String[]{"-s", "-S"});
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
        // the users.xml path; the database path is in testResetAdminPassword*
        Files.writeString(etcDir.resolve("opennms.properties"), "org.opennms.usermgmt.store=xml\n");

        UserFactory.init();
        final UserManager userManager = UserFactory.getInstance();
        userManager.reload();

        assertTrue(userManager.comparePasswords("admin", "admin"));
        try {
            installer.install(new String[]{"-R", "foobar", "-S"});
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

    /** Records which path resetWebAppAdminPassword takes, without a database or users.xml manager. */
    private static class RecordingInstaller extends Installer {
        private final List<String> m_calls = new ArrayList<>();
        private boolean m_adminMissing;

        RecordingInstaller() throws Exception {
            super();
        }

        @Override
        protected void setAdminPasswordInDatabase(final String password) {
            m_calls.add("db");
            if (m_adminMissing) {
                throw new IllegalArgumentException("User \"admin\" does not exist.");
            }
        }

        @Override
        protected void resetAdminPasswordInUsersXml(final String password) {
            m_calls.add("xml");
        }
    }

    private RecordingInstaller recordingInstaller() throws Exception {
        final RecordingInstaller recording = new RecordingInstaller();
        recording.m_opennms_home = opennmsHome.toString();
        recording.m_resetWebAppAdminPassword = "n3w";
        return recording;
    }

    @Test
    public void testResetAdminPasswordUsesTheDatabase() throws Exception {
        final RecordingInstaller recording = recordingInstaller();
        recording.resetWebAppAdminPassword();
        assertEquals(List.of("db"), recording.m_calls);
    }

    @Test
    public void testResetAdminPasswordFallsBackToUsersXmlBeforeTheMigration() throws Exception {
        // an upgrade from 36.x: -R runs before the migrator, which copies users.xml later
        Files.writeString(etcDir.resolve("users.xml"), "<userinfo/>");
        final RecordingInstaller recording = recordingInstaller();
        recording.m_adminMissing = true;
        recording.resetWebAppAdminPassword();
        assertEquals(List.of("db", "xml"), recording.m_calls);
    }

    @Test
    public void testResetAdminPasswordDoesNotUseUsersXmlAfterTheMigration() throws Exception {
        // the migrator keeps users.xml in etc, but nothing reads it any more: writing it would report a password
        // that the login never sees
        Files.writeString(etcDir.resolve("users.xml"), "<userinfo/>");
        Files.writeString(etcDir.resolve("opennms-upgrade-status.properties"), "UsersGroupsXmlMigratorOffline=Tue Oct 14 10\\:00\\:00 UTC 2026\n");
        final RecordingInstaller recording = recordingInstaller();
        recording.m_adminMissing = true;
        recording.resetWebAppAdminPassword();
        assertEquals(List.of("db"), recording.m_calls);
    }

    @Test
    public void testResetAdminPasswordWithoutAdminDoesNotFail() throws Exception {
        final RecordingInstaller recording = recordingInstaller();
        recording.m_adminMissing = true;
        recording.resetWebAppAdminPassword();
        assertEquals(List.of("db"), recording.m_calls);
    }

    @Test
    public void testResetAdminPasswordInXmlMode() throws Exception {
        Files.writeString(etcDir.resolve("opennms.properties"), "org.opennms.usermgmt.store=xml\n");
        final RecordingInstaller recording = recordingInstaller();
        recording.resetWebAppAdminPassword();
        assertEquals(List.of("xml"), recording.m_calls);
    }
}
