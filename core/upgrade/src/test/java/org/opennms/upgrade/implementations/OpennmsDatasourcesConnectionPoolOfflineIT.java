/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2024 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2024 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.test.xml.XmlTest;

public class OpennmsDatasourcesConnectionPoolOfflineIT {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File etcFolder;

    @Before
    public void before() throws Exception {
        etcFolder = temporaryFolder.newFolder("etc");
        // the `opennms-datasources.xml` file in here is from foundation-2023 before the new pool config was introduced
        FileUtils.copyDirectory(new File("src/test/resources/etc"), etcFolder);
        System.setProperty("opennms.home", temporaryFolder.getRoot().getAbsolutePath());
        XmlTest.initXmlUnit();
    }

    @Test
    public void testUpgrade() throws Exception {
        final var upgrader = new OpennmsDatasourcesConnectionPoolOffline();

        final var backupFile = new File(etcFolder, "opennms-datasources.xml.zip");
        assertFalse("backup file should not yet exist", backupFile.exists());
        upgrader.preExecute();
        assertTrue("backup file should have been created by pre-execution phase", backupFile.exists());

        final var expected = FileUtils.readFileToString(new File("src/test/resources/expected/etc/opennms-datasources.xml"), Charset.defaultCharset());
        upgrader.execute();
        final var actual = FileUtils.readFileToString(new File(etcFolder, "opennms-datasources.xml"), Charset.defaultCharset());

        final var diff = new DetailedDiff(XMLUnit.compareXML(expected, actual));
        final var differences = diff.getAllDifferences();
        for (final var difference : differences) {
            System.err.println("difference: " + difference);
        }
        assertEquals("there should be no differences between the converted and expected XML files", 0, differences.size());

        upgrader.postExecute();
        assertFalse("backup file should have been deleted by post-execution phase", backupFile.exists());
    }
}
