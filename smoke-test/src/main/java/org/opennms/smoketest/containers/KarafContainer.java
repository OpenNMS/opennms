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
package org.opennms.smoketest.containers;

import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.opennms.smoketest.utils.KarafShell;
import org.opennms.smoketest.utils.SshClient;
import org.opennms.smoketest.utils.TestContainerUtils;
import org.testcontainers.containers.Container;
import org.testcontainers.utility.MountableFile;

public interface KarafContainer<T extends KarafContainer<T>> extends Container<T> {

    /**
     * Create a new SSH client connected to the Karaf shell in this container.
     *
     * @return a new SSH client for this container
     */
    SshClient ssh();

    /**
     * Returns the socket address for the Karaf shell in this container.
     *
     * @return an InetSocketAddress usable for connecting to the Karaf shell.
     */
    InetSocketAddress getSshAddress();

    /**
     * Returns the path to the Karaf home directory.
     *
     * @return the path to the Karaf home directory
     */
    Path getKarafHomeDirectory();

    /**
     * Returns the path to the Karaf hot deploy directory. KARs installed in this directory will be
     * deployed automatically without needing to run "kar:install".
     *
     * @return the path to the Karaf hot deploy directory
     */
    default Path getKarafHotDeployDirectory() {
        return getKarafHomeDirectory().resolve("deploy");
    }

    /**
     * Copy KAR file from the host system to the Karaf hot deploy directory in the container,
     * verify that the feature is available, optionally run pre-install configuration commands,
     * and finally install the feature, ensuring there are no errors in the output.
     *
     * @param feature the name of the Karaf feature
     * @param kar the KAR file on the local host system to copy to the container
     * @param preInstallConfig pre-install configuration commands
     */
    default void installFeature(String feature, Path kar, String... preInstallConfig) {
        copyFileToContainer(MountableFile.forHostPath(kar), getKarafHotDeployDirectory().resolve(kar.getFileName()).toString());

        installFeature(feature, preInstallConfig);
    }

    /**
     * Verify that a Karaf feature is available, optionally run pre-install configuration commands,
     * and finally install the feature, ensuring there are no errors in the output.
     *
     * @param feature the name of the Karaf feature
     * @param preInstallConfig pre-install configuration commands
     */
    default void installFeature(String feature, String... preInstallConfig) {
        var karafShell = new KarafShell(getSshAddress());

        karafShell.runCommand("feature:list | grep " + feature,
                output -> output.contains(feature),false);

        for (var line : preInstallConfig) {
            assertTrue(karafShell.runCommandOnce(line,
                    output -> !output.toLowerCase().contains("error"), false));
        }

        // Note that the feature name doesn't always match the KAR name
        assertTrue(karafShell.runCommandOnce("feature:install " + feature,
                output -> !output.toLowerCase().contains("error"), false));
    }

    default void assertNoKarafDestroy(Path karafLogFile) {
        final var karafLogs = TestContainerUtils.getFileFromContainerAsString(this, karafLogFile);
        final var lines = karafLogs.split("[\r\n]+");

        final var regex = Pattern.compile("Destroying container");
        final var matches = new StringBuffer();
        for (final var line : lines) {
            if (regex.matcher(line).find()) {
                matches.append(line + "\n");
            }
        }

        if (matches.length() > 0) {
            throw new AssertionError("Found 'Destroying container' messages in karaf.log:\n" + matches);
        }
    }
}
