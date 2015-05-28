/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.rrd.RrdUtils;

public class FilesystemResourceStorageDaoTest {

    private FilesystemResourceStorageDao m_fsResourceStorageDao = new FilesystemResourceStorageDao();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        m_fsResourceStorageDao.setRrdDirectory(tempFolder.getRoot());
    }

    @Test
    public void exists() throws IOException {
        // Path is missing when the folder is missing
        assertFalse(m_fsResourceStorageDao.exists(ResourcePath.get("should", "not", "exist")));

        // Path is missing when the folder is empty
        File folder = tempFolder.newFolder("a");
        assertFalse(m_fsResourceStorageDao.exists(ResourcePath.get("a")));

        // Path is missing when it only contains an empty sub-folder
        File subFolder = tempFolder.newFolder("a", "b");
        assertFalse(m_fsResourceStorageDao.exists(ResourcePath.get("a")));

        // Path exists when the sub-folder contains an RRD file
        File rrd = new File(subFolder, "ds" + RrdUtils.getExtension());
        rrd.createNewFile();
        assertTrue(m_fsResourceStorageDao.exists(ResourcePath.get("a")));
        assertTrue(rrd.delete());

        // Path exists when the folder contains an RRD file
        rrd = new File(folder, "ds" + RrdUtils.getExtension());
        rrd.createNewFile();
        assertTrue(m_fsResourceStorageDao.exists(ResourcePath.get("a")));
    }

    @Test
    public void children() throws IOException {
        // Children are empty when the folder is missing
        assertEquals(0, m_fsResourceStorageDao.children(ResourcePath.get("should", "not", "exist")).size());

        // Children are empty when the folder is emtpy
        File folder = tempFolder.newFolder("a");
        assertEquals(0, m_fsResourceStorageDao.children(ResourcePath.get("a")).size());

        // Children are empty when the folder only contains an RRD file
        File rrd = new File(folder, "ds" + RrdUtils.getExtension());
        rrd.createNewFile();
        assertEquals(0, m_fsResourceStorageDao.children(ResourcePath.get("a")).size());
        assertTrue(rrd.delete());

        // Children are empty when the folder only contains an empty sub-folder
        File subFolder = tempFolder.newFolder("a", "b");
        assertEquals(0, m_fsResourceStorageDao.children(ResourcePath.get("a")).size());

        // Child exists when the sub-folder contains an RRD file
        rrd = new File(subFolder, "ds" + RrdUtils.getExtension());
        rrd.createNewFile();
        Set<ResourcePath> children = m_fsResourceStorageDao.children(ResourcePath.get("a"));
        assertEquals(1, children.size());
        assertEquals(ResourcePath.get("a", "b"), children.iterator().next());

        // Same call but specifying the depth
        children = m_fsResourceStorageDao.children(ResourcePath.get("a"), 1);
        assertEquals(1, children.size());
        assertEquals(ResourcePath.get("a", "b"), children.iterator().next());

        // No children when depth is 0
        assertEquals(0, m_fsResourceStorageDao.children(ResourcePath.get("a"), 0).size());
        assertTrue(rrd.delete());
    }
}
