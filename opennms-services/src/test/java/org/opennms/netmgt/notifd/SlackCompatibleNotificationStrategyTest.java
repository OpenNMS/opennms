/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
