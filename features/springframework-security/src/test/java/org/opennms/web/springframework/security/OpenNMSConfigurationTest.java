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
package org.opennms.web.springframework.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

import org.junit.Test;
import org.opennms.bootstrap.OpenNMSProxyLoginModule;

public class OpenNMSConfigurationTest {

    private static AppConfigurationEntry newEntry(String name) {
        return new AppConfigurationEntry(name, LoginModuleControlFlag.OPTIONAL, Collections.emptyMap());
    }

    private static final class MockConfiguration extends Configuration {
        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
            if ("realm1".equals(name)) {
                return new AppConfigurationEntry[] {newEntry("realm1")};
            } else if ("realm2".equals(name)) {
                return new AppConfigurationEntry[] {newEntry("realm2a")};
            }
            return null;
        }
    }

    private static final class KarafOsgiConfiguration extends Configuration {
        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
            if ("karaf".equals(name)) {
                return new AppConfigurationEntry[] {newEntry("karaf")};
            } else if ("realm2".equals(name)) {
                return new AppConfigurationEntry[] {newEntry("realm2b")};
            }
            return null;
        }
    }

    @Test
    public void canRetrieveAppConfigurationsFromMultipleConfigs() throws InterruptedException {
        // Set a mock config.
        MockConfiguration mockConfig = new MockConfiguration();
        Configuration.setConfiguration(mockConfig);

        // Now instantiate the OpenNMS configuration
        OpenNMSConfiguration onmsConfig = new OpenNMSConfiguration();
        onmsConfig.init();

        Thread.sleep(500);

        // Now set another config. that will be detected as the Karaf config.
        KarafOsgiConfiguration karafConfig = new KarafOsgiConfiguration();
        Configuration.setConfiguration(karafConfig);

        Thread.sleep(500);

        // Requests for "opennms" should be handled by the OpenNMSConfiguration
        AppConfigurationEntry[] entries = Configuration.getConfiguration().getAppConfigurationEntry("opennms");
        assertEquals(1, entries.length);
        assertEquals(OpenNMSProxyLoginModule.class.getName(), entries[0].getLoginModuleName());

        // Other requests should be passed to both the original and the Karaf configs.
        entries = Configuration.getConfiguration().getAppConfigurationEntry("realm1");
        assertEquals(1, entries.length);
        assertEquals("realm1", entries[0].getLoginModuleName());

        entries = Configuration.getConfiguration().getAppConfigurationEntry("realm2");
        assertEquals(2, entries.length);
        assertEquals("realm2a", entries[0].getLoginModuleName());
        assertEquals("realm2b", entries[1].getLoginModuleName());

        entries = Configuration.getConfiguration().getAppConfigurationEntry("karaf");
        assertEquals(1, entries.length);
        assertEquals("karaf", entries[0].getLoginModuleName());

        entries = Configuration.getConfiguration().getAppConfigurationEntry("!");
        assertNull(entries);
    }
}
