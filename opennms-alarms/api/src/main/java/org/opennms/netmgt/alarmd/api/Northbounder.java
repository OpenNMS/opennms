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
package org.opennms.netmgt.alarmd.api;

/**
 * North bound Interface API.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public interface Northbounder {

    /**
     * Start.
     *
     * @throws NorthbounderException the northbounder exception
     */
    public void start() throws NorthbounderException;

    /**
     * Used to determine if the northbounder is ready to accept alarms.
     *
     * If no northbounders are ready, the caller can save resources by not creating and
     * initializing the {@link NorthboundAlarm}s.
     *
     * This method is called once after northbounder is registered and started.
     * If the status were to change sometime after, the northbounder must re-register itself.
     *
     * @return <code>true</code> if the northbounder is ready to accept alarms, <code>false</code> otherwise.
     */
    boolean isReady();

    /**
     * On alarm.
     *
     * @param alarm the alarm
     * @throws NorthbounderException the northbounder exception
     */
    public void onAlarm(NorthboundAlarm alarm) throws NorthbounderException;

    /**
     * Stop.
     *
     * @throws NorthbounderException the northbounder exception
     */
    public void stop() throws NorthbounderException;

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName();

    /**
     * Reloads configuration.
     */
    public void reloadConfig() throws NorthbounderException;

}
