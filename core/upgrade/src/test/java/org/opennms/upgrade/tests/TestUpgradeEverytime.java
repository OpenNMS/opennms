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
package org.opennms.upgrade.tests;

import org.opennms.upgrade.api.OnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;
import org.opennms.upgrade.support.UpgradeHelper;

public class TestUpgradeEverytime implements OnmsUpgrade {
    @Override
    public int getOrder() {
        return 301;
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override
    public String getDescription() {
        return "Testing class that runs everytime";
    }

    @Override
    public void preExecute() throws OnmsUpgradeException {
    }

    @Override
    public void postExecute() throws OnmsUpgradeException {
    }

    @Override
    public void rollback() throws OnmsUpgradeException {
        UpgradeHelper.addRolledBack(getId());
    }

    @Override
    public void execute() throws OnmsUpgradeException {
        UpgradeHelper.addExecuted(getId());
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
