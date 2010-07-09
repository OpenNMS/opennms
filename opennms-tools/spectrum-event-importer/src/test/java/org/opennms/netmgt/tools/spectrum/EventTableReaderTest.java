/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 9, 2010
 *
 * Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.tools.spectrum;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.core.io.FileSystemResource;

/**
 * @author jeffg
 *
 */
public class EventTableReaderTest {
    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }
    
    @Test
    public void oneArgConstructor() throws IOException {
        @SuppressWarnings("unused")
        EventTableReader reader = new EventTableReader(new FileSystemResource("src/test/resources/sonus-traps/CsEvFormat/EventTables/ipUnityTrapSeverity"));
    }
    
    @Test
    public void readIpUnityTrapSeverityTable() throws IOException {
        EventTableReader reader = new EventTableReader(new FileSystemResource("src/test/resources/sonus-traps/CsEvFormat/EventTables/ipUnityTrapSeverity"));
        EventTable et = reader.getEventTable();
        
        Assert.assertEquals("There should exist 6 event-map entries in this EventTable file", 6, et.size());
        
        Assert.assertEquals("clear(1)", "clear", et.get(1));
        Assert.assertEquals("informational(2)", "informational", et.get(2));
        Assert.assertEquals("warning(3)", "warning", et.get(3));
        Assert.assertEquals("minor(4)", "minor", et.get(4));
        Assert.assertEquals("major(5)", "major", et.get(5));
        Assert.assertEquals("critical(6)", "critical", et.get(6));
    }
}
