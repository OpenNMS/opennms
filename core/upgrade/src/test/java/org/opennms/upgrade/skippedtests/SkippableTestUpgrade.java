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
package org.opennms.upgrade.skippedtests;

import org.opennms.upgrade.api.OnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;
import org.opennms.upgrade.support.UpgradeHelper;

/** A task for the JUnit tests of tasks that the upgrade skips, because OpenNMS is running or stopped. */
public abstract class SkippableTestUpgrade implements OnmsUpgrade {

    private final int m_order;
    private final boolean m_critical;
    private final boolean m_pending;
    private final boolean m_requiresOnmsRunning;

    protected SkippableTestUpgrade(final int order, final boolean critical, final boolean pending, final boolean requiresOnmsRunning) {
        m_order = order;
        m_critical = critical;
        m_pending = pending;
        m_requiresOnmsRunning = requiresOnmsRunning;
    }

    @Override
    public int getOrder() {
        return m_order;
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override
    public String getDescription() {
        return "Testing class - records its execution";
    }

    @Override
    public boolean isCritical() {
        return m_critical;
    }

    @Override
    public boolean isPending() {
        return m_pending;
    }

    @Override
    public boolean requiresOnmsRunning() {
        return m_requiresOnmsRunning;
    }

    @Override
    public void preExecute() throws OnmsUpgradeException {
    }

    @Override
    public void execute() throws OnmsUpgradeException {
        UpgradeHelper.addExecuted(getId());
    }

    @Override
    public void postExecute() throws OnmsUpgradeException {
    }

    @Override
    public void rollback() throws OnmsUpgradeException {
        UpgradeHelper.addRolledBack(getId());
    }
}
