/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.support;

import junit.framework.TestCase;

import org.opennms.netmgt.dao.api.GraphDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.RrdDao;
import org.opennms.test.FileAnticipator;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.EasyMockUtils;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class DefaultRrdGraphServiceTest extends TestCase {
    private EasyMockUtils m_mockUtils;
    private FileAnticipator m_fileAnticipator;
    
    private DefaultRrdGraphService m_service;

    private ResourceDao m_resourceDao;

    private GraphDao m_graphDao;

    private RrdDao m_rrdDao;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        m_mockUtils = new EasyMockUtils();
        m_fileAnticipator = new FileAnticipator(false);
        m_service = new DefaultRrdGraphService();
    }
    
    @Override
    protected void tearDown() throws Exception {
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
        
        m_mockUtils.replayAll();
        try {
            m_service.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
        m_mockUtils.verifyAll();
    }
    
    public void testNoGraphDao() {
        setUpResourceDao();
        setUpRrdDao();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("graphDao property has not been set"));
        
        m_mockUtils.replayAll();
        try {
            m_service.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
        m_mockUtils.verifyAll();
    }

    public void testNoRrdDao() {
        setUpResourceDao();
        setUpGraphDao();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("rrdDao property has not been set"));
        
        m_mockUtils.replayAll();
        try {
            m_service.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
        m_mockUtils.verifyAll();
    }
    
//    public void testLoadPropertiesNullWorkDir() {
//        ThrowableAnticipator ta = new ThrowableAnticipator();
//        ta.anticipate(new IllegalArgumentException("workDir argument cannot be null"));
//        try {
//            m_service.loadProperties(null, "foo");
//        } catch (Throwable t) {
//            ta.throwableReceived(t);
//        }
//        ta.verifyAnticipated();
//    }
//    
//    public void testLoadPropertiesNullPropertiesFile() {
//        ThrowableAnticipator ta = new ThrowableAnticipator();
//        ta.anticipate(new IllegalArgumentException("propertiesFile argument cannot be null"));
//        try {
//            m_service.loadProperties(new File(""), null);
//        } catch (Throwable t) {
//            ta.throwableReceived(t);
//        }
//        ta.verifyAnticipated();
//    }
//    
//    public void testLoadPropertiesEmpty() throws Exception {
//        m_fileAnticipator.initialize();
//        m_fileAnticipator.tempFile("strings.properties", "");
//        Properties p = m_service.loadProperties(m_fileAnticipator.getTempDir(), "strings.properties");
//        assertNotNull("properties should not be null", p);
//        assertEquals("properties size", 0, p.size());
//    }
//    
//    public void testLoadPropertiesNonEmpty() throws Exception {
//        m_fileAnticipator.initialize();
//        m_fileAnticipator.tempFile("strings.properties", "foo=bar");
//        Properties p = m_service.loadProperties(m_fileAnticipator.getTempDir(), "strings.properties");
//        assertNotNull("properties should not be null", p);
//        assertEquals("properties size", 1, p.size());
//        assertNotNull("property 'foo' should exist", p.get("foo"));
//        assertEquals("property 'foo' value", "bar", p.get("foo"));
//    }
//
//    public void testLoadPropertiesDoesNotExist() throws Exception {
//        m_fileAnticipator.initialize();
//
//        ThrowableAnticipator ta = new ThrowableAnticipator();
//        ta.anticipate(new ObjectRetrievalFailureException(Properties.class, "strings.properties", "This resource does not have a string properties file: " + new File(m_fileAnticipator.getTempDir(), "strings.properties").getAbsolutePath(), null));
//        try {
//            m_service.loadProperties(m_fileAnticipator.getTempDir(), "strings.properties");
//        } catch (Throwable t) {
//            ta.throwableReceived(t);
//        }
//        ta.verifyAnticipated();
//    }

    private void setUpAll() {
        setUpResourceDao();
        setUpGraphDao();
        setUpRrdDao();
        m_service.afterPropertiesSet();
    }
    
    private void setUpResourceDao() {
        m_resourceDao = m_mockUtils.createMock(ResourceDao.class);
        m_service.setResourceDao(m_resourceDao);
    }
    
    private void setUpGraphDao() {
        m_graphDao = m_mockUtils.createMock(GraphDao.class);
        m_service.setGraphDao(m_graphDao);
    }
    
    private void setUpRrdDao() {
        m_rrdDao = m_mockUtils.createMock(RrdDao.class);
        m_service.setRrdDao(m_rrdDao);
    }
    
}
