/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
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

import java.util.List;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.opennms.core.utils.Argument;
import org.opennms.netmgt.model.notifd.NotificationStrategy;

public class MicroblogReplyNotificationStrategyTest extends MicroblogNotificationStrategyTest {
    @Ignore
    @Test
    @Override
    public void postNotice() {
        NotificationStrategy ns = new MicroblogReplyNotificationStrategy(m_daoConfigResource);
        List<Argument> arguments = configureArgs();

        Assert.assertEquals("NotificationStrategy should return 0 on success", 0, ns.send(arguments));
    }
    
    @Override
    public List<Argument> configureArgs() {
        List<Argument> arguments = super.configureArgs();
        Argument arg = new Argument("-ublog", null, "jeffg", false);
        arguments.add(arg);
        return arguments;
    }

}
