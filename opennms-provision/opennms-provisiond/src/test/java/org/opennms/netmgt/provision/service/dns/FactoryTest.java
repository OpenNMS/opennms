/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.dns;

import static org.junit.Assert.*;

import java.net.URLStreamHandler;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.url.GenericURLFactory;
import org.opennms.core.utils.url.GenericURLStreamHandler;
import org.opennms.core.test.MockLogAppender;

public class FactoryTest {

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }

    @Test
    public void dwCreateURLStreamHandler() {

        GenericURLFactory.initialize();

        GenericURLFactory genericURLFactory = GenericURLFactory.getInstance();

        URLStreamHandler handler = genericURLFactory.createURLStreamHandler("abc");
        assertNull(handler);
        
        handler = genericURLFactory.createURLStreamHandler("dns");
        
        assertNotNull(handler);
        
        assertTrue(handler instanceof java.net.URLStreamHandler);
        assertTrue(handler instanceof GenericURLStreamHandler);
        
    }

}
