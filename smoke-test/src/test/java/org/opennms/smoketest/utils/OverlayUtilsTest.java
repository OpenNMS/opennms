/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.utils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.smoketest.stacks.OverlayFile;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import com.google.common.collect.Lists;

public class OverlayUtilsTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void canOverlayFilesAndFolders() throws IOException {
        File source = temporaryFolder.newFolder("source");

        File a = new File(source, "a");
        assertThat(a.createNewFile(), equalTo(true));
        File b = new File(source, "b");
        assertThat(b.mkdirs(), equalTo(true));
        File c = new File(b, "c");
        assertThat(c.createNewFile(), equalTo(true));

        File target = temporaryFolder.newFolder("target");

        OverlayUtils.copyFiles(Lists.newArrayList(new OverlayFile(a.toURI().toURL(), "a"),
                        new OverlayFile(b.toURI().toURL(), "b"),
                        new OverlayFile(c.toURI().toURL(), "c")),
                target.toPath());

        // Verify
        assertThat(target.toPath().resolve("a").toFile().isFile(), equalTo(true));
        assertThat(target.toPath().resolve("b").toFile().isDirectory(), equalTo(true));
        assertThat(target.toPath().resolve("b").resolve("c").toFile().isFile(), equalTo(true));
        assertThat(target.toPath().resolve("c").toFile().isFile(), equalTo(true));
    }
}
