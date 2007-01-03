package org.opennms.netmgt.dao.support;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import java.io.IOException;

import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.dao.support.DefaultResourceDao;
import org.opennms.netmgt.dao.support.ResponseTimeResourceType;
import org.opennms.netmgt.model.OnmsNode;

import junit.framework.TestCase;

public class ResponseTimeResourceTypeTest extends TestCase {
    public void testGetResourcesUsingDao() throws IOException {
        ResourceDao resourceDao = new DefaultResourceDao();
        NodeDao nodeDao = createMock(NodeDao.class);
        
        expect(nodeDao.get(1)).andReturn(new OnmsNode());
        
        ResponseTimeResourceType r = new ResponseTimeResourceType(resourceDao, nodeDao);
        
        replay(nodeDao);
        r.getResourcesForNode(1);
        verify(nodeDao);
    }
}
