/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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

        public Success(byte[] config, String filename) {
            this.config = config;
            this.filename = filename;
        }
    }

    class Failure {

        public final String message;
        public final Optional<String> stdout;
        public final Optional<String> stderr;

        public Failure(String message, Optional<String> stdout, Optional<String> stderr) {
            this.message = message;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public Failure(String message) {
            this.message = message;
            this.stdout = Optional.empty();
            this.stderr = Optional.empty();
        }
    }

}
