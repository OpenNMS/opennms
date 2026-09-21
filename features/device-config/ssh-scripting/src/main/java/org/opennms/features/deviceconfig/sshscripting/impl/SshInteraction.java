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
package org.opennms.features.deviceconfig.sshscripting.impl;

public interface SshInteraction {

    void sendLine(String string) throws Exception;

    void await(String string) throws Exception;

    /**
     * Marks the current position in the SSH session's stdout stream as the start of the
     * device configuration capture.  All output received after this point (up to the end
     * of the script) is returned in {@link org.opennms.features.deviceconfig.sshscripting.SshScriptingService.Result#capturedConfig}.
     */
    void startCapture() throws Exception;

    /**
     * Awaits the given string in the session output, then atomically marks the position
     * immediately after the match as the capture start point.  Unlike calling
     * {@link #await(String)} followed by {@link #startCapture()}, this method sets the
     * capture position while still holding the internal lock on the await buffer, so bytes
     * that arrive between the match and the capture-point record cannot push the start past
     * the actual config output.
     */
    void awaitAndCapture(String string) throws Exception;

    String replaceVars(String string);

}
