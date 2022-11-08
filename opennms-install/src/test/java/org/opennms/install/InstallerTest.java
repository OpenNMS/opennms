/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.install;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.bootstrap.FilesystemPermissionValidator;
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
}
