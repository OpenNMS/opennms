/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.test.MockLogAppender;
import org.springframework.core.io.ByteArrayResource;

public class PollOutagesConfigManagerTest extends TestCase {

    private PollOutagesConfigManager m_manager;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PollOutagesConfigManagerTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        MockLogAppender.setupLogging();
        
        String xml = "<?xml version=\"1.0\"?>\n" + 
                "<outages>\n" + 
                "   <outage name=\"one\" type=\"weekly\">\n" + 
                "       <time day=\"sunday\" begins=\"12:30:00\" ends=\"12:45:00\"/>\n" + 
                "       <time day=\"sunday\" begins=\"13:30:00\" ends=\"14:45:00\"/>\n" + 
                "       <time day=\"monday\" begins=\"13:30:00\" ends=\"14:45:00\"/>\n" + 
                "       <time day=\"tuesday\" begins=\"13:00:00\" ends=\"14:45:00\"/>\n" + 
                "       <interface address=\"192.168.0.1\"/>\n" + 
                "       <interface address=\"192.168.0.36\"/>\n" + 
                "       <interface address=\"192.168.0.38\"/>\n" + 
                "   </outage>\n" + 
                "\n" + 
                "   <outage name=\"two\" type=\"monthly\">\n" + 
                "       <time day=\"1\" begins=\"23:30:00\" ends=\"23:45:00\"/>\n" + 
                "       <time day=\"15\" begins=\"21:30:00\" ends=\"21:45:00\"/>\n" + 
                "       <time day=\"15\" begins=\"23:30:00\" ends=\"23:45:00\"/>\n" + 
                "       <interface address=\"192.168.100.254\"/>\n" + 
                "       <interface address=\"192.168.101.254\"/>\n" + 
                "       <interface address=\"192.168.102.254\"/>\n" + 
                "       <interface address=\"192.168.103.254\"/>\n" + 
                "       <interface address=\"192.168.104.254\"/>\n" + 
                "       <interface address=\"192.168.105.254\"/>\n" + 
                "       <interface address=\"192.168.106.254\"/>\n" + 
                "       <interface address=\"192.168.107.254\"/>\n" + 
                "   </outage>\n" + 
                "\n" + 
                "   <outage name=\"three\" type=\"specific\">\n" + 
                "       <time begins=\"21-Feb-2005 05:30:00\" ends=\"21-Feb-2005 15:00:00\"/>\n" + 
                "       <interface address=\"192.168.0.1\"/>\n" + 
                "   </outage>\n" + 
                "</outages>\n";
        
        m_manager = new PollOutagesConfigManager() {
            @Override
            public void update() throws IOException, MarshalException, ValidationException {}
        };

        m_manager.setConfigResource(new ByteArrayResource(xml.getBytes()));
        m_manager.afterPropertiesSet();
    }

    @Override
    protected void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
    }
    
    private long getTime(String timeString) throws ParseException {
        Date date = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse(timeString);
        return date.getTime();
        
    }
    
    public void testIsTimeInOutageWeekly() throws Exception {

        assertTrue(m_manager.isTimeInOutage(getTime("21-FEB-2005 14:00:00"), "one"));
        assertFalse(m_manager.isTimeInOutage(getTime("21-FEB-2005 14:00:00"), "two"));
        assertTrue(m_manager.isTimeInOutage(getTime("21-FEB-2005 14:00:00"), "three"));
        
        assertTrue(m_manager.isTimeInOutage(getTime("15-FEB-2005 14:00:00"), "one"));
        assertFalse(m_manager.isTimeInOutage(getTime("15-FEB-2005 14:00:00"), "two"));
        assertFalse(m_manager.isTimeInOutage(getTime("15-FEB-2005 14:00:00"), "three"));
        
        assertFalse(m_manager.isTimeInOutage(getTime("15-FEB-2005 23:37:00"), "one"));
        assertTrue(m_manager.isTimeInOutage(getTime("15-FEB-2005 23:37:00"), "two"));
        assertFalse(m_manager.isTimeInOutage(getTime("15-FEB-2005 23:37:00"), "three"));
        
        assertFalse(m_manager.isTimeInOutage(getTime("21-FEB-2005 16:00:00"), "one"));
        assertFalse(m_manager.isTimeInOutage(getTime("21-FEB-2005 16:00:00"), "two"));
        assertFalse(m_manager.isTimeInOutage(getTime("21-FEB-2005 16:00:00"), "three"));
        
        
    }


}
