/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

    public void remove(Object o) {
        m_mocks.remove(o);
    }
}
