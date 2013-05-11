/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.Argument;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
public class SnmpTrapNotificationStrategyTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLogAppender.setupLogging(true);
    }

    /*
     * Test method for 'org.opennms.netmgt.notifd.SnmpTrapNotificationStrategy.send(List)'
     */
    public void testSendWithEmptyArgumentList() {
        List<Argument> arguments = new ArrayList<Argument>();
        NotificationStrategy strategy = new SnmpTrapNotificationStrategy();
        strategy.send(arguments);

    }

    /*
     * Test method for 'org.opennms.netmgt.notifd.SnmpTrapNotificationStrategy.send(List)'
     */
    public void testSendWithNamedHost() {
        List<Argument> arguments = new ArrayList<Argument>();
        Argument arg = new Argument("trapHost", null, "localhost", false);
        arguments.add(arg);
        NotificationStrategy strategy = new SnmpTrapNotificationStrategy();
        strategy.send(arguments);

    }
    /*
     * Test method for 'org.opennms.netmgt.notifd.SnmpTrapNotificationStrategy.sendV1Trap()'
     */
    public void testSendV1Trap() {

    }

    /*
     * Test method for 'org.opennms.netmgt.notifd.SnmpTrapNotificationStrategy.sendV2Trap()'
     */
    public void testSendV2Trap() {

    }

}
