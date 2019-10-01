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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opennms.netmgt.model.notifd.Argument;

import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class SlackCompatibleNotificationStrategyTest {

    @Parameterized.Parameters
    public static Collection<Object[]> params() {
        return Arrays.asList(
            new Object[][] {
                { new MattermostNotificationStrategy(), "org.opennms.netmgt.notifd.mattermost" },
                { new SlackNotificationStrategy(), "org.opennms.netmgt.notifd.slack" }
        });
    }

    private final AbstractSlackCompatibleNotificationStrategy strategy;
    private final String systemPropertiesPrefix;

    public SlackCompatibleNotificationStrategyTest(final AbstractSlackCompatibleNotificationStrategy strategy, final String systemPropertiesPrefix) {
        this.strategy = strategy;
        this.systemPropertiesPrefix = systemPropertiesPrefix;
    }

    @Test
    public void verifyArgumentsWorkWithAndWithoutSlash() {
        final List<Argument> arguments = Lists.newArrayList();
        arguments.add(new Argument("-url", null, "http://localhost:1234/hooks/hooky", false));
        arguments.add(new Argument("-channel", null, "channel", false));
        arguments.add(new Argument("-username", null, "opennms", false));
        arguments.add(new Argument("-iconurl", null, "http://opennms.org/logo.png", false));
        arguments.add(new Argument("-iconemoji", null, ":shipit:", false));
        arguments.add(new Argument("-subject", null, "Test", false));
        arguments.add(new Argument("-tm", null, "This is only a test", false));

        strategy.setArguments(arguments);
        assertThat(strategy.getValue("url"), is("http://localhost:1234/hooks/hooky"));
        assertThat(strategy.getValue("-url"), is("http://localhost:1234/hooks/hooky"));

        assertThat(strategy.getValue("channel"), is("channel"));
        assertThat(strategy.getValue("-channel"), is("channel"));

        assertThat(strategy.getValue("username"), is("opennms"));
        assertThat(strategy.getValue("-username"), is("opennms"));

        assertThat(strategy.getValue("iconurl"), is("http://opennms.org/logo.png"));
        assertThat(strategy.getValue("-iconurl"), is("http://opennms.org/logo.png"));

        assertThat(strategy.getValue("iconemoji"), is(":shipit:"));
        assertThat(strategy.getValue("-iconemoji"), is(":shipit:"));

        assertThat(strategy.getValue("subject"), is("Test"));
        assertThat(strategy.getValue("-subject"), is("Test"));

        assertThat(strategy.getValue("tm"), is("This is only a test"));
        assertThat(strategy.getValue("-tm"), is("This is only a test"));
    }

    @Test
    public void verifyArgumentsFallbackToSystemProperty() {
        System.setProperty(getPropertyName("webhookURL"), "http://localhost:4321/hooks/abunchofstuffthatidentifiesawebhook");
        System.setProperty(getPropertyName("channel"), "integrationtestsXX");
        System.setProperty(getPropertyName("iconURL"), "http://opennms.org/logo.pngXX");
        System.setProperty(getPropertyName("iconEmoji"), ":shipitXX:");
        System.setProperty(getPropertyName("username"), "opennmsXX");

        final AbstractSlackCompatibleNotificationStrategy strategy = new MattermostNotificationStrategy();
        assertThat(strategy.getValue("-url", getPropertyName("webhookURL")), is("http://localhost:4321/hooks/abunchofstuffthatidentifiesawebhook"));
        assertThat(strategy.getValue("-channel", getPropertyName("channel")), is("integrationtestsXX"));
        assertThat(strategy.getValue("-iconurl", getPropertyName("iconURL")), is("http://opennms.org/logo.pngXX"));
        assertThat(strategy.getValue("-iconemoji", getPropertyName("iconEmoji")), is(":shipitXX:"));
        assertThat(strategy.getValue("-username", getPropertyName("username")), is("opennmsXX"));
    }

    private String getPropertyName(String name) {
        return String.format("%s.%s", systemPropertiesPrefix, name);
    }
}
