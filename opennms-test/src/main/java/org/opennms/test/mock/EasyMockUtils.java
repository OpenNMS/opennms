package org.opennms.test.mock;

import java.util.LinkedList;
import java.util.List;

import org.easymock.EasyMock;

/**
 * Utilities to support EasyMocking.
 *
 * @author ranger
 * @version $Id: $
 */
public class EasyMockUtils {
    private List<Object> m_mocks;
    
    /**
     * <p>Constructor for EasyMockUtils.</p>
     */
    public EasyMockUtils() {
        m_mocks = new LinkedList<Object>();
    }

    /**
     * <p>createMock</p>
     *
     * @param clazz a {@link java.lang.Class} object.
     * @param <T> a T object.
     * @return a T object.
     */
    public <T> T createMock(Class<T> clazz) {
        T object = EasyMock.createMock(clazz);
        m_mocks.add(object);
        return object;
    }
    
    /**
     * <p>replayAll</p>
     */
    public void replayAll() {
        for (Object o : m_mocks) {
            EasyMock.replay(o);
        }
    }
    
    /**
     * <p>verifyAll</p>
     */
    public void verifyAll() {
        for (Object o : m_mocks) {
            EasyMock.verify(o);
            EasyMock.reset(o);
        }
    }
}
