package org.opennms.netmgt.jasper.helper;

import static org.junit.Assert.assertEquals;

import java.io.File;
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
        
        assertEquals("/Users/thedesloge/git/opennms/integrations/opennms-jasper-extensions/src/test/resources/share/rrd/snmp/10/nsVpnMonitor/tun_id_1", paths.get(0));
        assertEquals("/Users/thedesloge/git/opennms/integrations/opennms-jasper-extensions/src/test/resources/share/rrd/snmp/10/nsVpnMonitor/tun_id_2", paths.get(1));
    }
    
    @Test
    public void testFindTopLevelOnlyIfPassesFilenameCheck() {
        ResourcePathFileTraversal traverser = new ResourcePathFileTraversal(new File(m_baseDir + "/" + m_nodeId + "/opennms-jvm"));
        traverser.addAndFilenameFilter("ThreadCount.jrb");
        traverser.addAndFilenameFilter("Bogus.jrb");
        List<String> paths = traverser.traverseDirectory();
        
        assertEquals(0, paths.size());
    }
    
    @Test
    public void testFindPathsWithFilterOneFile() {
        ResourcePathFileTraversal traverser = new ResourcePathFileTraversal(new File(m_baseDir + "/" + m_nodeId + "/" + m_resourceName));
        traverser.addAndFilenameFilter("http.dump");
        List<String> paths = traverser.traverseDirectory();
        
        
        assertEquals(2, paths.size());
        assertEquals("/Users/thedesloge/git/opennms/integrations/opennms-jasper-extensions/src/test/resources/share/rrd/snmp/10/nsVpnMonitor/tun_id_2", paths.get(0));
    }
    
    @Test
    public void testFindPathsWithAndFilter() {
        ResourcePathFileTraversal traverser = new ResourcePathFileTraversal(new File(m_baseDir + "/" + m_nodeId + "/" + m_resourceName));
        traverser.addAndFilenameFilter("http.dump").addAndFilenameFilter("icmp.jrb");
        List<String> paths = traverser.traverseDirectory();
        
        assertEquals(1, paths.size());
        assertEquals("/Users/thedesloge/git/opennms/integrations/opennms-jasper-extensions/src/test/resources/share/rrd/snmp/10/nsVpnMonitor/tun_id_3", paths.get(0));
    }
    
    @Test
    public void testFindPathsWithFilterNoExtensions() {
        ResourcePathFileTraversal traverser = new ResourcePathFileTraversal(new File(m_baseDir + "/" + m_nodeId + "/" + m_resourceName));
        traverser.addAndFilenameFilter("http").addAndFilenameFilter("icmp");
        List<String> paths = traverser.traverseDirectory();
        
        assertEquals(1, paths.size());
        assertEquals("/Users/thedesloge/git/opennms/integrations/opennms-jasper-extensions/src/test/resources/share/rrd/snmp/10/nsVpnMonitor/tun_id_3", paths.get(0));
    }
    
    @Test
    public void testFindPathsWithStoreByGroup() {
        System.setProperty("org.opennms.rrd.storeByGroup", "true");
        ResourcePathFileTraversal traverser = new ResourcePathFileTraversal(new File(m_baseDir + "/" + m_nodeId + "/storeby-group-jvm"));
        traverser.addDatasourceFilter("Bogus");
        List<String> paths = traverser.traverseDirectory();
        
        assertEquals(1, paths.size());
        assertEquals("/Users/thedesloge/git/opennms/integrations/opennms-jasper-extensions/src/test/resources/share/rrd/snmp/10/storeby-group-jvm", paths.get(0));
    }

}
