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
package org.opennms.core.password;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

import org.opennms.bootstrap.Bootstrap;
import org.opennms.netmgt.config.api.UserMgmtStore;

/**
 * Reads org.opennms.usermgmt.store for the tools that run without the OpenNMS system properties (the password CLI,
 * the installer): from the same files, in the same order, as Bootstrap loads them, with a system property winning.
 */
public final class UserMgmtStoreSetting {

    private UserMgmtStoreSetting() {
    }

    public static boolean isDatabase(final File opennmsHome) {
        final Properties properties = new Properties();
        for (final File file : Bootstrap.getPropertiesFiles(opennmsHome)) {
            if (!file.isFile()) {
                continue;
            }
            try (InputStream in = Files.newInputStream(file.toPath())) {
                properties.load(in);
            } catch (final IOException e) {
                throw new IllegalStateException("Cannot read " + file + ": " + e.getMessage(), e);
            }
        }
        final String override = System.getProperty(UserMgmtStore.PROPERTY);
        if (override != null) {
            properties.setProperty(UserMgmtStore.PROPERTY, override);
        }
        return UserMgmtStore.isDatabase(properties);
    }
}
