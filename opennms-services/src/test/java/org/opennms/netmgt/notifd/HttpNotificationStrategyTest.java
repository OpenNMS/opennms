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
 * by the Free Software Foundation, either version 2 of the License,
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

import java.util.ArrayList;
import java.util.List;

import org.opennms.core.utils.Argument;
import org.opennms.netmgt.mock.OpenNMSTestCase;

public class HttpNotificationStrategyTest extends OpenNMSTestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for 'org.opennms.netmgt.notifd.HttpNotificationStrategy.send(List)'
     */
    public void XXXtestSend() {
        
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
