package org.opennms.netmgt.jasper.helper;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Test;

public class FileTraversalTest {
    
    private String m_baseDir = "src/test/resources/share/rrd/snmp";
    private String m_resourceName = "nsVpnMonitor";
    private int m_nodeId = 10;
    
    @Test
    public void testFindAllPathsRecursive() {
        FileTraversal traverser = new FileTraversal(new File(m_baseDir + "/" + m_nodeId + "/" + m_resourceName));
        List<String> paths = traverser.traverseDirectory();
        
        assertEquals("/Users/thedesloge/git/opennms/integrations/opennms-jasper-extensions/src/test/resources/share/rrd/snmp/10/nsVpnMonitor/tun_id_1", paths.get(0));
        assertEquals("/Users/thedesloge/git/opennms/integrations/opennms-jasper-extensions/src/test/resources/share/rrd/snmp/10/nsVpnMonitor/tun_id_2", paths.get(1));
    }
    
    @Test
    public void testFindPathsWithFilterOneFile() {
        FileTraversal traverser = new FileTraversal(new File(m_baseDir + "/" + m_nodeId + "/" + m_resourceName));
        traverser.addNameFilter("http.dump");
        List<String> paths = traverser.traverseDirectory();
        
        
        assertEquals(2, paths.size());
        assertEquals("/Users/thedesloge/git/opennms/integrations/opennms-jasper-extensions/src/test/resources/share/rrd/snmp/10/nsVpnMonitor/tun_id_2", paths.get(0));
    }
    
    @Test
    public void testFindPathsWithAndFilter() {
        FileTraversal traverser = new FileTraversal(new File(m_baseDir + "/" + m_nodeId + "/" + m_resourceName));
        traverser.addAndNameFilter("http.dump").addAndNameFilter("icmp.jrb");
        List<String> paths = traverser.traverseDirectory();
        
        assertEquals(1, paths.size());
        assertEquals("/Users/thedesloge/git/opennms/integrations/opennms-jasper-extensions/src/test/resources/share/rrd/snmp/10/nsVpnMonitor/tun_id_3", paths.get(0));
    }
    
    @Test
    public void testFindPathsWithFilterNoExtensions() {
        FileTraversal traverser = new FileTraversal(new File(m_baseDir + "/" + m_nodeId + "/" + m_resourceName));
        traverser.addAndNameFilter("http").addAndNameFilter("icmp");
        List<String> paths = traverser.traverseDirectory();
        
        assertEquals(1, paths.size());
        assertEquals("/Users/thedesloge/git/opennms/integrations/opennms-jasper-extensions/src/test/resources/share/rrd/snmp/10/nsVpnMonitor/tun_id_3", paths.get(0));
    }

}
