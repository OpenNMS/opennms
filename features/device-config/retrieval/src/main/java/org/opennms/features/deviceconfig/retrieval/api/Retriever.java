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
package org.opennms.features.deviceconfig.retrieval.api;


import java.net.SocketAddress;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import io.vavr.control.Either;

public interface Retriever {

    /**
     * Asks devices to upload their configuration by executing the given script and accepts the uploaded data.
     * <p>
     * @return The returned completion stage is guaranteed to complete successfully, i.e. timeouts and all other
     * kinds of failures are returned as a {@code Failure} instances.
     */
    CompletionStage<Either<Failure, Success>> retrieveConfig(
            Protocol protocol,
            String script,
            String user,
            String password,
            String authKey,
            final SocketAddress target,
            final String hostKeyFingerprint,
            String shell,
            String configType,
            Map<String, String> vars,
            Duration timeout
    );

    enum Protocol {
        TFTP, FTP, SCP
    }

    class Success {

        public final byte[] config;
        public final String filename;
        public final String scriptOutput;

        public Success(byte[] config, String filename) {
            this(config, filename, "");
        }

        public Success(byte[] config, String fileName, String scriptOutput) {
            this.config = config;
            this.filename = fileName;
            this.scriptOutput = scriptOutput;
        }
    }

    class Failure {

        public final String message;
        public final Optional<String> stdout;
        public final Optional<String> stderr;

        public final String scriptOutput;

        public Failure(String message, Optional<String> stdout, Optional<String> stderr) {
            this(message, stdout, stderr, "");
        }

        public Failure(String message, Optional<String> stdout, Optional<String> stderr, String scriptOutput) {
            this.message = message;
            this.stdout = stdout;
            this.stderr = stderr;
            this.scriptOutput = scriptOutput;
        }

        public Failure(String message) {
            this.message = message;
            this.stdout = Optional.empty();
            this.stderr = Optional.empty();
            this.scriptOutput = "";
        }
    }

}
