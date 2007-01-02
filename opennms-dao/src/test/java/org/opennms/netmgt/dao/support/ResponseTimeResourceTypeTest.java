package org.opennms.netmgt.dao.support;

import java.io.IOException;

import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.dao.support.DefaultResourceDao;
import org.opennms.netmgt.dao.support.ResponseTimeResourceType;

import junit.framework.TestCase;

public class ResponseTimeResourceTypeTest extends TestCase {
    public void testGetResourcesUsingDao() throws IOException {
        ResourceDao resourceDao = new DefaultResourceDao();
        
        ResponseTimeResourceType r = new ResponseTimeResourceType(resourceDao, null);
        
        r.getResourcesForNode(1);
    }
}
