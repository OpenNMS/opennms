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
package org.opennms.core.spring;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Test loading real beanRefContext.xml with parent-child relationships
 */
public class BeanRefContextTest {
    
    @Before
    public void setUp() {
        // Reload contexts to pick up our test beanRefContext.xml
        ContextRegistry.getInstance().reloadContexts();
    }
    
    @Test
    public void testContextsAreLoaded() {
        ContextRegistry registry = ContextRegistry.getInstance();
        
        // Check all contexts from our test beanRefContext.xml are loaded
        assertTrue("Should have daemonContext", registry.hasContext("daemonContext"));
        assertTrue("Should have daoContext", registry.hasContext("daoContext"));
        assertTrue("Should have alarmdContext", registry.hasContext("alarmdContext"));
    }
    
    @Test
    public void testDaemonContextLoads() {
        ContextRegistry registry = ContextRegistry.getInstance();
        
        BeanFactoryReference ref = registry.getBeanFactory("daemonContext");
        assertNotNull("daemonContext reference should not be null", ref);
        
        ApplicationContext context = (ApplicationContext) ref.getFactory();
        assertNotNull("daemonContext should not be null", context);
        
        // Should have no parent (root context)
        assertNull("daemonContext should have no parent", context.getParent());
        
        // Should contain the bean from applicationContext-daemon.xml
        assertTrue("Should contain daemonService bean", context.containsBean("daemonService"));
        String service = context.getBean("daemonService", String.class);
        assertEquals("Daemon Service Bean", service);
        
        ref.release();
    }
    
    @Test
    public void testDaoContextWithParent() {
        ContextRegistry registry = ContextRegistry.getInstance();
        
        // Get dao context
        BeanFactoryReference daoRef = registry.getBeanFactory("daoContext");
        ApplicationContext daoContext = (ApplicationContext) daoRef.getFactory();
        assertNotNull("daoContext should not be null", daoContext);
        
        // Should have parent context
        ApplicationContext parent = daoContext.getParent();
        assertNotNull("daoContext should have a parent", parent);
        
        // Parent should be daemonContext
        assertTrue("Parent should contain daemonService", parent.containsBean("daemonService"));
        
        // Should contain its own bean
        assertTrue("daoContext should contain daoService", daoContext.containsBean("daoService"));
        String daoService = daoContext.getBean("daoService", String.class);
        assertEquals("DAO Service Bean", daoService);
        
        // Should be able to access parent beans
        assertTrue("daoContext should access parent beans", daoContext.containsBean("daemonService"));
        String parentService = daoContext.getBean("daemonService", String.class);
        assertEquals("Daemon Service Bean", parentService);
        
        daoRef.release();
    }
    
    @Test 
    public void testBeanUtilsWithRealContexts() {
        // Test BeanUtils.getBeanFactory with our real contexts
        BeanFactoryReference ref = BeanUtils.getBeanFactory("daemonContext");
        assertNotNull("BeanUtils should return reference", ref);
        
        ApplicationContext context = (ApplicationContext) ref.getFactory();
        assertTrue("Should contain daemonService", context.containsBean("daemonService"));
        
        ref.release();
    }
}