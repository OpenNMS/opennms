/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.core.soa.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.soa.ServiceRegistry;
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
public class ReferenceFactoryBeanTest {
    
    @Autowired
    ServiceRegistry serviceRegistry;
    
    @Autowired
    Hello hello;
    
    @Autowired
    Goodbye goodbye;
    
    @Test
    @DirtiesContext
    public void testFindReference() throws IOException {
        
        assertNotNull(serviceRegistry);
        assertNotNull(hello);
        assertNotNull(goodbye);

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
