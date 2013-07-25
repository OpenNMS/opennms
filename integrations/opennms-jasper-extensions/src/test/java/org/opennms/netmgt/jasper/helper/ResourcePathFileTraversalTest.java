/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.jasper.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ResourcePathFileTraversalTest {
    
    private static final String STORE_BY_GROUP = "org.opennms.rrd.storeByGroup";
    private String m_sourceDir = "target/test-classes/share/rrd/snmp";
    private String m_baseDir = "target/file-traversal-test";
    private String m_resourceName = "nsVpnMonitor";
    private int m_nodeId = 10;

    @Before
    public void setUp() throws IOException {
        final File baseDir = new File(m_baseDir);
        FileUtils.copyDirectory(new File(m_sourceDir), baseDir);
        System.setProperty(STORE_BY_GROUP, "false");
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(new File(m_baseDir));
    }

    @Test
    public void testFindAllPathsRecursive() {
        final ResourcePathFileTraversal traverser = new ResourcePathFileTraversal(new File(m_baseDir + "/" + m_nodeId + "/" + m_resourceName));
        final List<String> paths = traverser.traverseDirectory();

        Collections.sort(paths);
        assertTrue(paths.get(0).matches(".*/10/nsVpnMonitor/tun_id_1"));
        assertTrue(paths.get(1).matches(".*/10/nsVpnMonitor/tun_id_2"));
    }
    
    @Test
    public void testFindTopLevelOnlyIfPassesFilenameCheck() {
        final ResourcePathFileTraversal traverser = new ResourcePathFileTraversal(new File(m_baseDir + "/" + m_nodeId + "/opennms-jvm"));
        traverser.addDatasourceFilter("ThreadCount");
        traverser.addDatasourceFilter("Bogus");
        final List<String> paths = traverser.traverseDirectory();
        assertEquals(0, paths.size());
    }
    
    @Test
    public void testFindPathsWithFilterOneFile() {
        final ResourcePathFileTraversal traverser = new ResourcePathFileTraversal(new File(m_baseDir + "/" + m_nodeId + "/" + m_resourceName));
        traverser.addDatasourceFilter("http-8980");
        final List<String> paths = traverser.traverseDirectory();
        
        Collections.sort(paths);
        assertEquals(2, paths.size());
        assertTrue(paths.get(0).matches(".*/10/nsVpnMonitor/tun_id_2"));
    }
    
    @Test
    public void testFindPathsWithAndFilter() {
        final ResourcePathFileTraversal traverser = new ResourcePathFileTraversal(new File(m_baseDir + "/" + m_nodeId + "/" + m_resourceName));
        traverser.addDatasourceFilter("http");
        traverser.addDatasourceFilter("icmp");
        final List<String> paths = traverser.traverseDirectory();
        
        Collections.sort(paths);
        assertEquals(1, paths.size());
        assertTrue(paths.get(0).matches(".*/10/nsVpnMonitor/tun_id_3"));
    }
    
    @Test
    public void testFindPathsWithFilterNoExtensions() {
        final ResourcePathFileTraversal traverser = new ResourcePathFileTraversal(new File(m_baseDir + "/" + m_nodeId + "/" + m_resourceName));
        traverser.addDatasourceFilter("http");
        traverser.addDatasourceFilter("icmp");
        final List<String> paths = traverser.traverseDirectory();
        
        Collections.sort(paths);
        assertEquals(1, paths.size());
        assertTrue(paths.get(0).matches(".*/10/nsVpnMonitor/tun_id_3"));
    }
    
    @Test
    public void testFindPathsWithStoreByGroup() {
        System.setProperty(STORE_BY_GROUP, "true");
        final ResourcePathFileTraversal traverser = new ResourcePathFileTraversal(new File(m_baseDir + "/" + m_nodeId + "/storeby-group-jvm"));
        traverser.addDatasourceFilter("TotalMemory");
        List<String> paths = traverser.traverseDirectory();
        
        Collections.sort(paths);
        assertEquals(1, paths.size());
        assertTrue(paths.get(0).matches(".*/10/storeby-group-jvm"));
        
        ResourcePathFileTraversal traverser2 = new ResourcePathFileTraversal(new File(m_baseDir + "/" + m_nodeId + "/storeby-group-jvm"));
        traverser2.addDatasourceFilter("Bogus");
        paths = traverser2.traverseDirectory();
        
        assertEquals(0, paths.size());
    }

}
