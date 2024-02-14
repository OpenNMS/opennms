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
package org.opennms.features.deviceconfig.sshscripting;

import java.net.SocketAddress;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

public interface SshScriptingService {

    /**
     * Executes the given script and optionally returns a failure.
     * <p>
     * @param script contains statements separated by new lines
     * @param user the ssh user
     * @param password the ssh user's password
     * @param authKey the private ssh key
     * @param target the ssh target to connect to
     * @param hostKeyFingerprint host key fingerprint to accept from the target system
     * @param shell optional shell to spawn on connection
     * @param vars variables that can be referenced in the script; variables are referenced by "{@code ${varname}}" expressions
     * @param timeout used when establishing the ssh interaction and for await statements
     * @return
     */
    Result execute(
            String script,
            String user,
            String password,
            final String authKey,
            final SocketAddress target,
            final String hostKeyFingerprint,
            final String shell,
            Map<String, String> vars,
            Duration timeout
    );

    String getScriptOutput();

    class Result {
        public final String message;
        public final Optional<String> stdout;
        public final Optional<String> stderr;
        public final boolean success;

        public final String scriptOutput;

        private Result(final boolean success, final String message, final Optional<String> stdout, final Optional<String> stderr, String scriptOutput) {
            this.success = success;
            this.message = message;
            this.stdout = stdout;
            this.stderr = stderr;
            this.scriptOutput = scriptOutput;
        }

        public static Result success(final String message, final String stdout, final String stderr, String scriptOutput) {
            return new Result(true, message, Optional.of(stdout), Optional.of(stderr), scriptOutput);
        }

        public static Result success(final String message, final String stdout, final String stderr) {
            return new Result(true, message, Optional.of(stdout), Optional.of(stderr), "");
        }

        public static Result success(final String message) {
            return new Result(true, message, Optional.empty(), Optional.empty(), "");
        }

        public static Result failure(final String message, final String stdout, final String stderr, String scriptOutput) {
            return new Result(false, message, Optional.of(stdout), Optional.of(stderr), scriptOutput);
        }

        public static Result failure(final String message, final String stdout, final String stderr) {
            return new Result(false, message, Optional.of(stdout), Optional.of(stderr), "");
        }

        public static Result failure(final String message) {
            return new Result(false, message, Optional.empty(), Optional.empty(), "");
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isFailed() {
            return !success;
        }
    }
}
