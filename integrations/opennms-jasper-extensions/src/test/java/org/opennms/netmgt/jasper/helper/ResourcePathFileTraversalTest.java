package org.opennms.netmgt.jasper.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class ResourcePathFileTraversalTest {
    
    private String m_baseDir = "src/test/resources/share/rrd/snmp";
    private String m_resourceName = "nsVpnMonitor";
    private int m_nodeId = 10;
    
    @Test
    public void testFindAllPathsRecursive() {
        ResourcePathFileTraversal traverser = new ResourcePathFileTraversal(new File(m_baseDir + "/" + m_nodeId + "/" + m_resourceName));
        List<String> paths = traverser.traverseDirectory();
        
        Collections.sort(paths);
        assertTrue(paths.get(0).matches(".*src/test/resources/share/rrd/snmp/10/nsVpnMonitor/tun_id_1"));
        assertTrue(paths.get(1).matches(".*src/test/resources/share/rrd/snmp/10/nsVpnMonitor/tun_id_2"));
    }
    
    @Test
    public void testFindTopLevelOnlyIfPassesFilenameCheck() {
        ResourcePathFileTraversal traverser = new ResourcePathFileTraversal(new File(m_baseDir + "/" + m_nodeId + "/opennms-jvm"));
        traverser.addDatasourceFilter("ThreadCount");
        traverser.addDatasourceFilter("Bogus");
        List<String> paths = traverser.traverseDirectory();
        
        assertEquals(0, paths.size());
    }
    
    @Test
    public void testFindPathsWithFilterOneFile() {
        ResourcePathFileTraversal traverser = new ResourcePathFileTraversal(new File(m_baseDir + "/" + m_nodeId + "/" + m_resourceName));
        traverser.addDatasourceFilter("http-8980");
        List<String> paths = traverser.traverseDirectory();
        
        
        Collections.sort(paths);
        assertEquals(2, paths.size());
        assertTrue(paths.get(0).matches(".*src/test/resources/share/rrd/snmp/10/nsVpnMonitor/tun_id_2"));
    }
    
    @Test
    public void testFindPathsWithAndFilter() {
        ResourcePathFileTraversal traverser = new ResourcePathFileTraversal(new File(m_baseDir + "/" + m_nodeId + "/" + m_resourceName));
        traverser.addDatasourceFilter("http");
        traverser.addDatasourceFilter("icmp");
        List<String> paths = traverser.traverseDirectory();
        
        Collections.sort(paths);
        assertEquals(1, paths.size());
        assertTrue(paths.get(0).matches(".*src/test/resources/share/rrd/snmp/10/nsVpnMonitor/tun_id_3"));
    }
    
    @Test
    public void testFindPathsWithFilterNoExtensions() {
        ResourcePathFileTraversal traverser = new ResourcePathFileTraversal(new File(m_baseDir + "/" + m_nodeId + "/" + m_resourceName));
        traverser.addDatasourceFilter("http");
        traverser.addDatasourceFilter("icmp");
        List<String> paths = traverser.traverseDirectory();
        
        Collections.sort(paths);
        assertEquals(1, paths.size());
        assertTrue(paths.get(0).matches(".*src/test/resources/share/rrd/snmp/10/nsVpnMonitor/tun_id_3"));
    }
    
    @Test
    public void testFindPathsWithStoreByGroup() {
        System.setProperty("org.opennms.rrd.storeByGroup", "true");
        ResourcePathFileTraversal traverser = new ResourcePathFileTraversal(new File(m_baseDir + "/" + m_nodeId + "/storeby-group-jvm"));
        traverser.addDatasourceFilter("TotalMemory");
        List<String> paths = traverser.traverseDirectory();
        
        Collections.sort(paths);
        assertEquals(1, paths.size());
        assertTrue(paths.get(0).matches(".*src/test/resources/share/rrd/snmp/10/storeby-group-jvm"));
        
        ResourcePathFileTraversal traverser2 = new ResourcePathFileTraversal(new File(m_baseDir + "/" + m_nodeId + "/storeby-group-jvm"));
        traverser2.addDatasourceFilter("Bogus");
        paths = traverser2.traverseDirectory();
        
        assertEquals(0, paths.size());

    }

}
