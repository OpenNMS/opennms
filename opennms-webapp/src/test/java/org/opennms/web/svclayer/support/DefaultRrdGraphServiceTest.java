/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.svclayer.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.opennms.netmgt.dao.api.GraphDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.RrdDao;
import org.opennms.test.FileAnticipator;
import org.opennms.test.ThrowableAnticipator;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class DefaultRrdGraphServiceTest extends TestCase {
    private FileAnticipator m_fileAnticipator;
    
    private DefaultRrdGraphService m_service;

    private ResourceDao m_resourceDao;

    private GraphDao m_graphDao;

    private RrdDao m_rrdDao;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        m_fileAnticipator = new FileAnticipator(false);
        m_service = new DefaultRrdGraphService();
    }
    
    @Override
    protected void tearDown() throws Exception {
        if (m_resourceDao != null) verifyNoMoreInteractions(m_resourceDao);
        if (m_graphDao != null)    verifyNoMoreInteractions(m_graphDao);
        m_fileAnticipator.tearDown();
    }
    
    public void testAfterPropertiesSet() {
        setUpAll();
    }

    public void testNoResourceDao() throws Exception {
        setUpGraphDao();
        setUpRrdDao();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("resourceDao property has not been set"));
        
        try {
            m_service.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testNoGraphDao() {
        setUpResourceDao();
        setUpRrdDao();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("graphDao property has not been set"));
        
        try {
            m_service.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    public void testNoRrdDao() {
        setUpResourceDao();
        setUpGraphDao();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("rrdDao property has not been set"));

        try {
            m_service.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    private void setUpAll() {
        setUpResourceDao();
        setUpGraphDao();
        setUpRrdDao();
        m_service.afterPropertiesSet();
    }
    
    private void setUpResourceDao() {
        m_resourceDao = mock(ResourceDao.class);
        m_service.setResourceDao(m_resourceDao);
    }
    
    private void setUpGraphDao() {
        m_graphDao = mock(GraphDao.class);
        m_service.setGraphDao(m_graphDao);
    }
    
    private void setUpRrdDao() {
        m_rrdDao = mock(RrdDao.class);
        m_service.setRrdDao(m_rrdDao);
    }
    
}
