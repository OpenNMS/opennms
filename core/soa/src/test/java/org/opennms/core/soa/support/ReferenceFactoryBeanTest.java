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
package org.opennms.core.soa.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.soa.ServiceRegistry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * ReferenceFactoryBeanTest
 *
 * @author brozow
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ReferenceFactoryBeanTest implements InitializingBean {
    
    @Autowired
    ServiceRegistry serviceRegistry;
    
    @Autowired
    Hello hello;
    
    @Autowired
    Goodbye goodbye;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        assertNotNull(serviceRegistry);
        assertNotNull(hello);
        assertNotNull(goodbye);
    }
    
    @Test
    @DirtiesContext
    public void testFindReference() throws IOException {
        
        MyProvider provider = new MyProvider();
        
        serviceRegistry.register(provider, Hello.class, Goodbye.class);
        
        assertEquals(0, provider.helloSaid());
        
        hello.sayHello();
        
        assertEquals(1, provider.helloSaid());
        
        assertEquals(0, provider.goodbyeSaid());
        
        goodbye.sayGoodbye();
        
        assertEquals(1, provider.goodbyeSaid());
        
    }

}
