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
package org.opennms.features.topology.app.internal;

import org.opennms.features.topology.api.AutoRefreshSupport;

public class DefaultAutoRefreshSupport implements AutoRefreshSupport {

    private boolean enabled = false;
    private long interval = 60;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean value) {
        enabled = value;
    }

    @Override
    public boolean toggle() {
        enabled = !enabled;
        return isEnabled();
    }

    @Override
    public void setInterval(long secondsToWait) {
        // We do not allow to set < 5 Seconds
        if (secondsToWait < 5) {
            secondsToWait = 5;
        }
        interval = secondsToWait;
    }

    @Override
    public long getInterval() {
        return interval;
    }
}
