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
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy;

public class FilesystemResourceStorageDaoTest {

    private FilesystemResourceStorageDao m_fsResourceStorageDao = new FilesystemResourceStorageDao();

    private String m_rrdFileExtension;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        RrdStrategy<?, ?> rrdStrategy = new JRobinRrdStrategy();
        m_rrdFileExtension = rrdStrategy.getDefaultFileExtension();

        m_fsResourceStorageDao.setRrdDirectory(tempFolder.getRoot());
        m_fsResourceStorageDao.setRrdStrategy(rrdStrategy);
    }

    @Test
    public void exists() throws IOException {
        // Path is missing when the folder is missing
        assertFalse(m_fsResourceStorageDao.exists(ResourcePath.get("should", "not", "exist"), 0));

        // Path is missing when the folder is empty
        File folder = tempFolder.newFolder("a");
        assertFalse(m_fsResourceStorageDao.exists(ResourcePath.get("a"), 0));

        // Path is missing when it only contains an empty sub-folder
        File subFolder = tempFolder.newFolder("a", "b");
        assertFalse(m_fsResourceStorageDao.exists(ResourcePath.get("a"), 1));

        // Path exists when the sub-folder contains an RRD file
        File rrd = new File(subFolder, "ds" + m_rrdFileExtension);
        rrd.createNewFile();
        assertTrue(m_fsResourceStorageDao.exists(ResourcePath.get("a"), 1));
        assertTrue(rrd.delete());

        // Path exists when the folder contains an RRD file
        rrd = new File(folder, "ds" + m_rrdFileExtension);
        rrd.createNewFile();
        assertTrue(m_fsResourceStorageDao.exists(ResourcePath.get("a"), 0));
    }

    @Test
    public void existsWithin() throws IOException {
        File folder = tempFolder.newFolder("a", "b", "c");
        File rrd = new File(folder, "ds" + m_rrdFileExtension);
        rrd.createNewFile();

        assertTrue(m_fsResourceStorageDao.exists(ResourcePath.get("a", "b"), 1));
        assertTrue(m_fsResourceStorageDao.existsWithin(ResourcePath.get("a", "b"), 1));

        assertTrue(m_fsResourceStorageDao.exists(ResourcePath.get("a"), 2));
        assertFalse(m_fsResourceStorageDao.existsWithin(ResourcePath.get("a"), 1));
        assertTrue(m_fsResourceStorageDao.existsWithin(ResourcePath.get("a"), 2));
        assertTrue(m_fsResourceStorageDao.existsWithin(ResourcePath.get("a"), 3));
    }

    @Test
    public void children() throws IOException {
        // Children are empty when the folder is missing
        assertEquals(0, m_fsResourceStorageDao.children(ResourcePath.get("should", "not", "exist"), 1).size());

        // Children are empty when the folder is emtpy
        File folder = tempFolder.newFolder("a");
        assertEquals(0, m_fsResourceStorageDao.children(ResourcePath.get("a"), 1).size());

        // Children are empty when the folder only contains an RRD file
        File rrd = new File(folder, "ds" + m_rrdFileExtension);
        rrd.createNewFile();
        assertEquals(0, m_fsResourceStorageDao.children(ResourcePath.get("a"), 1).size());
        assertTrue(rrd.delete());

        // Children are empty when the folder only contains an empty sub-folder
        File subFolder = tempFolder.newFolder("a", "b");
        assertEquals(0, m_fsResourceStorageDao.children(ResourcePath.get("a"), 1).size());

        // Child exists when the sub-folder contains an RRD file
        rrd = new File(subFolder, "ds" + m_rrdFileExtension);
        rrd.createNewFile();
        Set<ResourcePath> children = m_fsResourceStorageDao.children(ResourcePath.get("a"), 1);
        assertEquals(1, children.size());
        assertEquals(ResourcePath.get("a", "b"), children.iterator().next());

        // Same call but specifying the depth
        children = m_fsResourceStorageDao.children(ResourcePath.get("a"), 1);
        assertEquals(1, children.size());
        assertEquals(ResourcePath.get("a", "b"), children.iterator().next());

        // No children when depth is 0
        assertTrue(rrd.delete());
    }

    @Test
    public void getAttributes() throws IOException {
        File subFolder = tempFolder.newFolder("a");
        assertFalse(m_fsResourceStorageDao.exists(ResourcePath.get("a"), 0));

        File rrd = new File(subFolder, "ds" + m_rrdFileExtension);
        rrd.createNewFile();
        assertTrue(m_fsResourceStorageDao.exists(ResourcePath.get("a"), 0));

        Set<OnmsAttribute> attributes = m_fsResourceStorageDao.getAttributes(ResourcePath.get("a"));
        assertEquals(1, attributes.size());
    }
}
