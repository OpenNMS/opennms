/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.Argument;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class HttpNotificationStrategyTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    /*
     * Test method for 'org.opennms.netmgt.notifd.HttpNotificationStrategy.send(List)'
     */
    @Test
    @Ignore
    public void testSend() {
        
        try {
        NotificationStrategy ns = new HttpNotificationStrategy();
        List<Argument> arguments = new ArrayList<Argument>();
        Argument arg = null;
        arg = new Argument("url", null, "http://172.16.8.68/cgi-bin/noauth/nmsgw.pl", false);
        arguments.add(arg);
        arg = new Argument("post-NodeID", null, "1", false);
        arguments.add(arg);
//        arg = new Argument("post-event", null, "199", false);
//        arguments.add(arg);
//        arg = new Argument("post-nasid", null, "1", false);
//        arguments.add(arg);
//        arg = new Argument("post-message", null, "JUnit Test RT Integration", false);
//        arguments.add(arg);
        
//        arg = new Argument("result-match", null, ".*OK\\s([0-9]+)\\s.*", false);
        arg = new Argument("result-match", null, "(?s).*OK\\s+([0-9]+).*", false);
        arguments.add(arg);
        
        arg = new Argument("post-message", null, "-tm", false);
        arguments.add(arg);
        
        arg = new Argument("-tm", null, "text message for unit testing", false);
        arguments.add(arg);
        
        arg = new Argument("sql", null, "UPDATE alarms SET tticketID=${1} WHERE lastEventID = 1", false);
        arguments.add(arg);
        
        ns.send(arguments);
        } catch (Throwable e) {
            e.printStackTrace();
            fail("Caught Exception:");
        }
    }

}
