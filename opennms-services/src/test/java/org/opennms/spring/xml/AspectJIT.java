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
package org.opennms.spring.xml;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.model.ImmutableMapper;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/org/opennms/spring/xml/applicationContext-testAOP.xml"
})
@JUnitConfigurationEnvironment
@DirtiesContext
public class AspectJIT implements InitializingBean {
    
    @Autowired
    private MockEventIpcManager m_eventIpcManager;
    
    @Autowired
    private AspectJITEventHandler m_handler;
    
    @Autowired
    private AspectJITEventHandlerInteceptor m_interceptor;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void onSetUp() throws Exception {
        m_handler.reset();
        m_interceptor.reset();
    }

    @Test
    public void testAOPProxying() throws Throwable {
        
        assertEquals(0, m_handler.getHandlerCallCount());
        assertEquals(0, m_interceptor.getPreEventCount());
        assertEquals(0, m_interceptor.getPostEventCount());
        
        m_handler.handleAnEvent(ImmutableMapper.fromMutableEvent(
                createEvent(EventConstants.NODE_LOST_SERVICE_EVENT_UEI)));
        
        assertEquals(1, m_handler.getHandlerCallCount());
        assertEquals(1, m_interceptor.getPreEventCount());
        assertEquals(1, m_interceptor.getPostEventCount());
        
    }
    
    @Test
    public void testEventAdapterOnProxy() {
        
        assertEquals(0, m_handler.getHandlerCallCount());
        assertEquals(0, m_interceptor.getPreEventCount());
        assertEquals(0, m_interceptor.getPostEventCount());
        
        sendEvent(EventConstants.NODE_LOST_SERVICE_EVENT_UEI);
        
        assertEquals(1, m_handler.getHandlerCallCount());
        assertEquals(1, m_interceptor.getPreEventCount());
        assertEquals(1, m_interceptor.getPostEventCount());
        
    }
    
    @Test
    public void testHandledException() {
        
        assertEquals(0, m_handler.getHandlerCallCount());
        assertEquals(0, m_interceptor.getPreEventCount());
        assertEquals(0, m_interceptor.getPostEventCount());
        assertEquals(0, m_interceptor.getHandledExceptionCount());
        
        m_handler.setThrownException(new RuntimeException("test exception"));
        
        sendEvent(EventConstants.NODE_LOST_SERVICE_EVENT_UEI);
        
        assertEquals(1, m_handler.getHandlerCallCount());
        assertEquals(1, m_interceptor.getPreEventCount());
        assertEquals(0, m_interceptor.getPostEventCount());
        assertEquals(1, m_interceptor.getHandledExceptionCount());
        
    }
    
    @Test
    public void testUnhandledException() {
        
        assertEquals(0, m_handler.getHandlerCallCount());
        assertEquals(0, m_interceptor.getPreEventCount());
        assertEquals(0, m_interceptor.getPostEventCount());
        assertEquals(0, m_interceptor.getHandledExceptionCount());
        
        m_handler.setThrownException(new Exception("test exception"));
        
        sendEvent(EventConstants.NODE_LOST_SERVICE_EVENT_UEI);
        
        assertEquals(1, m_handler.getHandlerCallCount());
        assertEquals(1, m_interceptor.getPreEventCount());
        assertEquals(0, m_interceptor.getPostEventCount());
        assertEquals(0, m_interceptor.getHandledExceptionCount());
        
    }
    
    private void sendEvent(String uei) {
        m_eventIpcManager.sendNow(createEvent(uei));
    }

    private Event createEvent(String uei) {
        return new EventBuilder(uei, "Test").getEvent();
    }
}
