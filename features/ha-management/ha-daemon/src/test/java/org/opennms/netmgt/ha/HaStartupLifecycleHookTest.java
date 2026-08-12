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
package org.opennms.netmgt.ha;

import org.junit.Test;
import org.opennms.netmgt.vmmgr.StartupLifecycleHook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ServiceLoader;

import static org.junit.Assert.assertTrue;

public class HaStartupLifecycleHookTest {

    @Test
    public void discoveredViaServiceLoader() {
        boolean found = false;
        for (StartupLifecycleHook hook : ServiceLoader.load(StartupLifecycleHook.class)) {
            if (hook instanceof HaStartupLifecycleHook) {
                found = true;
            }
        }
        assertTrue("ServiceLoader must discover HaStartupLifecycleHook", found);
    }

    @Test
    public void proceedsWhenHaConfigAbsent() throws Exception {
        Path tempEtc = Files.createTempDirectory("ha-hook-test").resolve("etc");
        Files.createDirectories(tempEtc);
        String oldHome = System.getProperty("opennms.home");
        System.setProperty("opennms.home", tempEtc.getParent().toString());
        try {
            assertTrue("no HA config means plain startup",
                    new HaStartupLifecycleHook().awaitReadyToStart());
        } finally {
            if (oldHome != null) System.setProperty("opennms.home", oldHome);
            else System.clearProperty("opennms.home");
        }
    }
}
