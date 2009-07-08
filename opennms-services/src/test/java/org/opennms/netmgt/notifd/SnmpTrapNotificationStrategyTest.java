/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2005-2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.notifd;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.opennms.core.utils.Argument;
import org.opennms.test.mock.MockLogAppender;
public class SnmpTrapNotificationStrategyTest extends TestCase {

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
