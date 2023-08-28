/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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
}
