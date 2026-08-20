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
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;

/**
 * As of NMS-19841, {@code java.io.tmpdir} moved from {@code $OPENNMS_HOME/data/tmp} to
 * {@code $OPENNMS_HOME/tmp}, and {@code activemq.data} moved from
 * {@code $OPENNMS_HOME/data/tmp/activemq} to {@code $OPENNMS_HOME/var/activemq}. Both were
 * previously located inside Karaf's cache directory ({@code $OPENNMS_HOME/data}), which is
 * pruned on every upgrade by {@link ClearKarafCacheMigratorOffline} (NMS-16226) and can also be
 * wiped via {@code karaf.clean.all}.
 * <p>This must run before {@link ClearKarafCacheMigratorOffline} so the existing ActiveMQ
 * KahaDB store and any Drools-persisted KIE session state (see
 * {@code DroolsCorrelationEngine}/{@code DroolsNorthbounder}, which store
 * {@code opennms.drools.*.state} files under {@code java.io.tmpdir}) survive the transition
 * instead of being silently deleted along with the rest of the cache.</p>
 */
public class RelocateJvmScratchDataMigratorOffline extends AbstractOnmsUpgrade {

    private static final FilenameFilter DROOLS_STATE_FILE_FILTER =
            (dir, name) -> name.startsWith("opennms.drools.") && name.endsWith(".state");

    private final File oldTmpDir;
    private final File oldActivemqDir;
    private final File newTmpDir;
    private final File newActivemqDir;

    public RelocateJvmScratchDataMigratorOffline() throws OnmsUpgradeException {
        final File home = new File(ConfigFileConstants.getHome());
        this.oldTmpDir = new File(home, "data/tmp");
        this.oldActivemqDir = new File(this.oldTmpDir, "activemq");
        this.newTmpDir = new File(home, "tmp");
        this.newActivemqDir = new File(home, "var/activemq");
    }

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    public String getDescription() {
        return String.format("Relocates the ActiveMQ store and any persisted Drools session state "
                + "from '%s' to '%s' and '%s', see NMS-19841", this.oldTmpDir, this.newTmpDir, this.newActivemqDir);
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
        try {
            FileUtils.forceMkdir(this.newTmpDir);

            if (this.oldActivemqDir.isDirectory() && !this.newActivemqDir.exists()) {
                log("Moving ActiveMQ store from '%s' to '%s'\n", this.oldActivemqDir, this.newActivemqDir);
                FileUtils.forceMkdir(this.newActivemqDir.getParentFile());
                FileUtils.moveDirectory(this.oldActivemqDir, this.newActivemqDir);
            }

            if (this.oldTmpDir.isDirectory()) {
                final File[] stateFiles = this.oldTmpDir.listFiles(DROOLS_STATE_FILE_FILTER);
                if (stateFiles != null) {
                    for (final File stateFile : stateFiles) {
                        final File target = new File(this.newTmpDir, stateFile.getName());
                        log("Moving Drools session state from '%s' to '%s'\n", stateFile, target);
                        FileUtils.deleteQuietly(target);
                        FileUtils.moveFile(stateFile, target);
                    }
                }
            }
        } catch (IOException e) {
            throw new OnmsUpgradeException(String.format("Error relocating JVM scratch data from '%s'.", this.oldTmpDir), e);
        }
    }

    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }

    @Override
    public boolean runOnlyOnce() {
        return true;
    }
}
