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
package org.opennms.netmgt.threshd.api;

/**
 * An interface for tracking and reinitializing the in-memory values of thresholding states.
 */
public interface ThresholdStateMonitor {
    /**
     * Track the given state identified by the given key.
     */
    void trackState(String key, ReinitializableState state);

    /**
     * Run some arbitrary code while holding the lock to the state monitor. This is used to block reinitialization while
     * the given {@link Runnable code} is running.
     */
    void withReadLock(Runnable r);

    /**
     * Reinitialize a single state identified by the given key.
     */
    void reinitializeState(String stateKey);

    /**
     * Reinitialize all states currently tracked by this monitor.
     */
    void reinitializeStates();
}
