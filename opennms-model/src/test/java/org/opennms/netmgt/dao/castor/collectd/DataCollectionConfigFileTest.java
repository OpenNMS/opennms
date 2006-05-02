package org.opennms.netmgt.dao.castor.collectd;

import java.io.IOException;

import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.dao.castor.collector.DataCollectionConfigFile;
import org.opennms.netmgt.dao.castor.collector.DataCollectionVisitor;
import org.springframework.core.io.ClassPathResource;

import junit.framework.TestCase;

public class DataCollectionConfigFileTest extends TestCase {

    private final class TopLevelCountingVisitor implements DataCollectionVisitor {
        boolean visited = false;
        boolean completed = false;
        public void completeDataCollectionConfig(DatacollectionConfig dataCollectionConfig) {
            completed = true;
        }

        public void visitDataCollectionConfig(DatacollectionConfig dataCollectionConfig) {
            visited = true;
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testVisitTop() throws IOException {
        ClassPathResource resource = new ClassPathResource("/datacollection-config.xml");
        DataCollectionConfigFile configFile = new DataCollectionConfigFile(resource.getFile());
        
        TopLevelCountingVisitor visitor = new TopLevelCountingVisitor();
        
        assertTrue(visitor.visited);
        assertTrue(visitor.completed);
    }

}
