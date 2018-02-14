/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.notifd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.model.notifd.Argument;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class IrcCatNotificationStrategyTest {
    /**
     * This doesn't really do anything, but it's a placeholder so that the testSend() test can be left disabled.
     */
    @Test
    public void testInstantiate() {
        new IrcCatNotificationStrategy();
    }
    
    //@Test
    public void testSend() throws UnknownHostException {
        IrcCatNotificationStrategy strategy = new IrcCatNotificationStrategy();
        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument(NotificationManager.PARAM_EMAIL, null, "#opennms-test", false));
        arguments.add(new Argument(NotificationManager.PARAM_TEXT_MSG, null, "Test notification from " + getClass() + " from " + InetAddress.getLocalHost(), false));
        strategy.send(arguments);
    }
}
 