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
package org.opennms.upgrade.implementations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;

public class ClearKarafCacheMigratorOffline  extends AbstractOnmsUpgrade {

    private final Path opennmsDataPath;

    public ClearKarafCacheMigratorOffline() throws OnmsUpgradeException {
         this.opennmsDataPath = Path.of(ConfigFileConstants.getHome()).resolve("data");
    }

    @Override
    public int getOrder() {
        return 16;
    }

    @Override
    public String getDescription() {
        return String.format("Clears the Karaf cache in '%s', see NMS-16226", this.opennmsDataPath);
    }

    @Override
    public void preExecute() throws OnmsUpgradeException {
    }

    @Override
    public void postExecute() throws OnmsUpgradeException {
    }

    @Override
    public void rollback() throws OnmsUpgradeException {
    }

    @Override
    public void execute() throws OnmsUpgradeException {
        final Path historyFilePath = this.opennmsDataPath.resolve("history.txt");
        try {
            Files.walk(this.opennmsDataPath)
                    .filter((path) -> !path.equals(historyFilePath) && !path.equals(this.opennmsDataPath))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new OnmsUpgradeException(String.format("Error pruning Karaf's data directory '%s'.", this.opennmsDataPath), e);
        }
    }

    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }

    @Override
    public boolean runOnlyOnce() {
        return false;
    }
}
