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

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Test class for ContextRegistry
 */
public class ContextRegistryTest {
    
    @Test
    public void testContextRegistration() {
        ContextRegistry registry = ContextRegistry.getInstance();
        
        // Create a test context
        ClassPathXmlApplicationContext testContext = new ClassPathXmlApplicationContext();
        testContext.refresh();
        
        // Register it
        registry.registerContext("testContext", testContext);
        
        // Verify we can retrieve it
        assertTrue(registry.hasContext("testContext"));
        
        // Get a reference
        BeanFactoryReference ref = registry.getBeanFactory("testContext");
        assertNotNull(ref);
        
        BeanFactory factory = ref.getFactory();
        assertNotNull(factory);
        assertEquals(testContext, factory);
        
        // Release the reference
        ref.release();
        
        // Context should still be available
        assertTrue(registry.hasContext("testContext"));
        
        testContext.close();
    }
    
    @Test
    public void testBeanUtilsCompatibility() {
        // Create a test context
        ClassPathXmlApplicationContext testContext = new ClassPathXmlApplicationContext();
        testContext.refresh();
        
        // Register it
        ContextRegistry.getInstance().registerContext("testBeanUtilsContext", testContext);
        
        // Test BeanUtils.getBeanFactory
        BeanFactoryReference ref =
            BeanUtils.getBeanFactory("testBeanUtilsContext");
        assertNotNull(ref);
        
        BeanFactory factory = ref.getFactory();
        assertNotNull(factory);
        assertEquals(testContext, factory);
        
        ref.release();
        testContext.close();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNonExistentContext() {
        ContextRegistry.getInstance().getBeanFactory("nonExistentContext");
    }
}