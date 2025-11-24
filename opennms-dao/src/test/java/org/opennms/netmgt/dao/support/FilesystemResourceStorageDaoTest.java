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
import org.opennms.netmgt.rrd.rrdtool.MultithreadedJniRrdStrategy;

public class FilesystemResourceStorageDaoTest {

    private FilesystemResourceStorageDao m_fsResourceStorageDao = new FilesystemResourceStorageDao();

    private String m_rrdFileExtension;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        RrdStrategy<?, ?> rrdStrategy = new MultithreadedJniRrdStrategy();
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
