/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
