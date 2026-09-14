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
package org.opennms.features.scv.cli.commands;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.MapOptionHandler;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.cli.ScvCli;

public class SetCommand implements Function<ScvCli, Integer> {
    @Argument(required = true,
            index = 0,
            metaVar = "alias",
            usage = "the alias for this entry")
    private String alias = null;

    @Argument(required = true,
            index = 1,
            metaVar = "username",
            usage = "the username to be set")
    private String username = null;

    @Argument(required = false,
            index = 2,
            metaVar = "password",
            usage = "the password to be set; if omitted, it is read from --from-stdin or prompted for interactively")
    private String password = null;

    @Option(name="--attribute",
            aliases = {"-a"},
            handler = MapOptionHandler.class)
    Map<String,String> attributes = new HashMap<>();

    @Option(name = "--from-stdin",
            aliases = {"-S"},
            usage = "read the password from standard input instead of the command line")
    private boolean fromStdin = false;

    @Override
    public Integer apply(ScvCli scvCli) {
        Objects.requireNonNull(alias);

        if (alias.equalsIgnoreCase(Credentials.GET_ALL_ALIAS)) {
            throw new IllegalArgumentException("Cannot set credentials using alias '" + Credentials.GET_ALL_ALIAS + "'.");
        }

        scvCli.getSecureCredentialsVault().setCredentials(alias, new Credentials(username, resolvePassword(), attributes));
        return 0;
    }

    /**
     * Determines the password to set, in order of precedence:
     * <ol>
     *   <li>the {@code password} positional argument, when supplied;</li>
     *   <li>standard input, when {@code --from-stdin} is given;</li>
     *   <li>an interactive prompt otherwise.</li>
     * </ol>
     *
     * <p>Package-visible for testing.</p>
     */
    String resolvePassword() {
        if (password != null) {
            if (fromStdin) {
                System.err.println("Warning: a password argument was provided; ignoring --from-stdin.");
            }
            return password;
        }
        if (fromStdin) {
            return readPasswordFromStdin();
        }
        return promptForPassword();
    }

    private static String readPasswordFromStdin() {
        try {
            // Note: deliberately not closing the reader, as that would close System.in.
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            final String line = reader.readLine();
            if (line == null) {
                throw new IllegalStateException("expected a password on standard input, but none was provided");
            }
            return line;
        } catch (final IOException e) {
            throw new RuntimeException("failed to read password from standard input", e);
        }
    }

    private static String promptForPassword() {
        final Console console = System.console();
        int count = 0;
        if (console == null) {
            // No interactive console (e.g. input is piped/redirected); fall back to reading stdin.
            return readPasswordFromStdin();
        }
        while (true) {
            final char[] first = console.readPassword("Enter password: ");
            final char[] second = console.readPassword("Confirm password: ");
            if (first == null || second == null) {
                throw new IllegalStateException("no password entered");
            }
            try {
                if (Arrays.equals(first, second)) {
                    return new String(first);
                }
                System.err.println("Passwords do not match. Please try again.");
                count++;
            } finally {
                Arrays.fill(first, '\0');
                Arrays.fill(second, '\0');
            }
            if (count >= 5) {
                System.err.println("Too many mismatches, giving up!");
                return null;
            }
        }
    }
}
