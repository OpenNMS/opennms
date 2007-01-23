package org.opennms.test.mock;

import java.util.LinkedList;
import java.util.List;

import org.easymock.EasyMock;

/**
 * Utilities to support EasyMocking.  
 */
public class EasyMockUtils {
    private List<Object> m_mocks;
    
    public EasyMockUtils() {
        m_mocks = new LinkedList<Object>();
    }

    public <T> T createMock(Class<T> clazz) {
        T object = EasyMock.createMock(clazz);
        m_mocks.add(object);
        return object;
    }
    
    public void replayAll() {
        for (Object o : m_mocks) {
            EasyMock.replay(o);
        }
    }
    
    public void verifyAll() {
        for (Object o : m_mocks) {
            EasyMock.verify(o);
            EasyMock.reset(o);
        }
    }
}