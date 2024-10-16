/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.upgrade.implementations;

import static org.junit.Assert.assertThat;

import java.nio.file.Path;

import org.hamcrest.core.Is;
import org.hamcrest.text.StringContainsInOrder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.upgrade.api.OnmsUpgradeException;

import com.google.common.collect.Lists;

public class ClearKarafCacheMigratorOfflineIT {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void before() throws Exception {
        temporaryFolder.newFolder("data", "aaa");
        temporaryFolder.newFolder("data", "bbb");
        temporaryFolder.newFolder("etc");
        temporaryFolder.newFile("data/history.txt");
        temporaryFolder.newFile("data/foo.bar");
        temporaryFolder.newFile("etc/opennms.properties");
        temporaryFolder.newFile("etc/rrd-configuration.properties");
        System.setProperty("opennms.home", temporaryFolder.getRoot().getAbsolutePath());
    }

    @Test
    public void testDeletion() {
        final Path temporaryPath = temporaryFolder.getRoot().toPath();
        assertThat("temporary folder must exist", temporaryPath.toFile().exists(), Is.is(true));
        assertThat("data directory must exist", temporaryPath.resolve("data").toFile().exists(), Is.is(true));
        assertThat("data/history.txt file must exist", temporaryPath.resolve("data").resolve("history.txt").toFile().exists(), Is.is(true));
        assertThat("data/foo.bar file must exist", temporaryPath.resolve("data").resolve("foo.bar").toFile().exists(), Is.is(true));
        assertThat("data/aaa directory must exist", temporaryPath.resolve("data").resolve("aaa").toFile().exists(), Is.is(true));
        assertThat("data/bbb directory must exist", temporaryPath.resolve("data").resolve("bbb").toFile().exists(), Is.is(true));
        assertThat("etc/opennms.properties file must exist", temporaryPath.resolve("etc").resolve("opennms.properties").toFile().exists(), Is.is(true));
        assertThat("etc/rrd-configuration.properties file must exist", temporaryPath.resolve("etc").resolve("rrd-configuration.properties").toFile().exists(), Is.is(true));

        try {
            new ClearKarafCacheMigratorOffline().execute();
        } catch (OnmsUpgradeException e) {
            assertThat("Exception message must match `Karaf's data directory ... pruned'", e.getMessage(), StringContainsInOrder.stringContainsInOrder(Lists.newArrayList("Karaf's data directory", "pruned")));
        }

        assertThat("temporary folder must exist", temporaryPath.toFile().exists(), Is.is(true));
        assertThat("data directory must exist", temporaryPath.resolve("data").toFile().exists(), Is.is(true));
        assertThat("data/history.txt file must exist", temporaryPath.resolve("data").resolve("history.txt").toFile().exists(), Is.is(true));
        assertThat("data/foo.bar file must not exist", temporaryPath.resolve("data").resolve("foo.bar").toFile().exists(), Is.is(false));
        assertThat("data/aaa directory must not exist", temporaryPath.resolve("data").resolve("aaa").toFile().exists(), Is.is(false));
        assertThat("data/bbb directory must not exist", temporaryPath.resolve("data").resolve("bbb").toFile().exists(), Is.is(false));
        assertThat("etc/opennms.properties file must exist", temporaryPath.resolve("etc").resolve("opennms.properties").toFile().exists(), Is.is(true));
        assertThat("etc/rrd-configuration.properties file must exist", temporaryPath.resolve("etc").resolve("rrd-configuration.properties").toFile().exists(), Is.is(true));
    }
}
