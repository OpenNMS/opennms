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

package org.opennms.features.deviceconfig.sshscripting;

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
     * @param host the ssh host
     * @param port the ssh port
     * @param vars variables that can be referenced in the script; variables are referenced by "{@code ${varname}}" expressions
     * @param timeout used when establishing the ssh interaction and for await statements
     * @return
     */
    Optional<Failure> execute(
            String script,
            String user,
            String password,
            String host,
            int port,
            Map<String, String> vars,
            Duration timeout
    );

    class Failure {

        public final String message;
        public final Optional<String> stdout;
        public final Optional<String> stderr;

        public Failure(String message, Optional<String> stdout, Optional<String> stderr) {
            this.message = message;
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }

}
