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
package org.opennms.netmgt.vmmgr;

/**
 * Hook into the OpenNMS process lifecycle for optionally-installed modules,
 * discovered via {@link java.util.ServiceLoader}. When no provider jar is on
 * the classpath the lifecycle proceeds as if the hook did not exist.
 *
 * <p>Contract: {@link Starter} calls {@link #awaitReadyToStart()} before the
 * service Invoker runs — it may block indefinitely (e.g. a standby waiting
 * for promotion), and any exception it throws aborts startup. {@code Manager}
 * calls {@link #onShutdownRequested()} when a stop begins (before services
 * drain) and {@link #onServicesStopped()} after the last service has stopped.
 */
public interface StartupLifecycleHook {

    /**
     * Blocks until this process is cleared to start its services.
     *
     * @return {@code true} to proceed with service startup, {@code false} to
     *         exit cleanly without starting services
     */
    boolean awaitReadyToStart();

    /** A stop has been requested; services have not drained yet. */
    void onShutdownRequested();

    /** All services have stopped. */
    void onServicesStopped();
}
