/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.model.notifd.Argument;

import com.google.common.collect.Lists;

public class SlackCompatibleNotificationStrategyTest {

    @Test
    public void verifyArguments() {
        final List<Argument> arguments = Lists.newArrayList();
        arguments.add(new Argument("-url", null, "url", false));
        arguments.add(new Argument("-channel", null, "channel", false));
        arguments.add(new Argument("-username", null, "opennms", false));
        arguments.add(new Argument("-iconurl", null, "http://opennms.org/logo.png", false));
        arguments.add(new Argument("-iconemoji", null, ":shipit:", false));
        arguments.add(new Argument("-subject", null, "Test", false));
        arguments.add(new Argument("-tm", null, "This is only a test", false));

        final AbstractSlackCompatibleNotificationStrategy mattermostNotificationStrategy = new MattermostNotificationStrategy();
        Assert.assertThat(mattermostNotificationStrategy.getUrl(), Matchers.is("url"));
        mattermostNotificationStrategy.getChannel();
        mattermostNotificationStrategy.getIconEmoji();
        mattermostNotificationStrategy.getIconUrl();
        mattermostNotificationStrategy.getUsername();
    }
}
