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
package org.opennms.core.wsman.utils;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.opennms.core.wsman.shell.CommandResult;
import org.opennms.core.wsman.shell.ShellOptions;

import com.google.common.base.Strings;

/**
 * Helpers shared by the WinRS shell monitor and detector: argument handling,
 * banner matching, exit code checks and shell option handling.
 */
public final class ShellCommandUtils {

    /** Banner value that matches any output, as in the other monitors. */
    public static final String BANNER_ANY = "*";

    /** Prefix that marks a banner as a regular expression, as in the other monitors and policies. */
    public static final String REGEX_PREFIX = "~";

    /** Exit code value that disables the exit code check. */
    public static final String EXIT_CODE_ANY = "*";

    /** Longest output excerpt included in a failure reason. */
    static final int MAX_EXCERPT = 512;

    private ShellCommandUtils() {
    }

    /**
     * Converts the configured argument string into the arguments handed to WinRS.
     *
     * <p>WinRS builds the remote command line by joining the command and its arguments
     * with spaces, and neither it nor the client library adds any quoting. The
     * argument string is therefore passed through verbatim as a single argument, so
     * what is written in the configuration is exactly the command line tail the
     * Windows host executes, with Windows quoting rules applying. In an XML
     * configuration file double quotes are written as {@code &quot;}, which the XML
     * parser turns back into plain quotes before the value gets here.
     *
     * @param args the raw argument string, may be null or blank
     * @return an empty array when there are no arguments, otherwise the trimmed string as the only element
     */
    public static String[] toArguments(String args) {
        if (args == null || args.trim().isEmpty()) {
            return new String[0];
        }
        return new String[] { args.trim() };
    }

    /**
     * Checks command output against a banner. A null, empty or {@code *} banner matches
     * anything. A banner starting with {@code ~} is a regular expression that must match
     * the whole output; it is compiled with {@link Pattern#DOTALL} so {@code .} spans
     * lines. Any other banner must appear somewhere in the output.
     *
     * @throws PatternSyntaxException if a regex banner is invalid
     */
    public static boolean bannerMatches(String banner, String output) {
        if (Strings.isNullOrEmpty(banner) || BANNER_ANY.equals(banner)) {
            return true;
        }
        final String text = output == null ? "" : output;
        if (banner.startsWith(REGEX_PREFIX)) {
            return Pattern.compile(banner.substring(REGEX_PREFIX.length()), Pattern.DOTALL).matcher(text).matches();
        }
        return text.contains(banner);
    }

    /**
     * Checks a command's exit code against the expected value. {@code *} (or null/empty)
     * accepts any exit code.
     *
     * @throws NumberFormatException if the expected value is neither {@code *} nor an integer
     */
    public static boolean exitCodeMatches(String expected, int actual) {
        if (Strings.isNullOrEmpty(expected) || EXIT_CODE_ANY.equals(expected)) {
            return true;
        }
        return Integer.parseInt(expected.trim()) == actual;
    }

    /**
     * Builds the {@link ShellOptions} from the optional monitor/detector settings.
     */
    public static ShellOptions buildShellOptions(boolean noProfile, Integer codepage, String workingDirectory) {
        final ShellOptions.Builder builder = new ShellOptions.Builder().withNoProfile(noProfile);
        if (codepage != null) {
            builder.withCodepage(codepage);
        }
        if (!Strings.isNullOrEmpty(workingDirectory)) {
            builder.withWorkingDirectory(workingDirectory);
        }
        return builder.build();
    }

    /**
     * Evaluates a command result against the expected exit code and banner.
     *
     * @return an empty optional when the result is acceptable, otherwise the reason it is not
     */
    public static Optional<String> checkResult(CommandResult result, String expectedExitCode, String banner) {
        if (!exitCodeMatches(expectedExitCode, result.exitCode())) {
            return Optional.of(String.format("Command exited with code %d, expected %s. stderr: '%s' stdout: '%s'",
                    result.exitCode(), expectedExitCode, excerpt(result.stderr()), excerpt(result.stdout())));
        }
        if (!bannerMatches(banner, result.stdout())) {
            return Optional.of(String.format("Banner '%s' not matched by command output: '%s'",
                    banner, excerpt(result.stdout())));
        }
        return Optional.empty();
    }

    /**
     * Trims output for inclusion in a status reason or log message.
     */
    public static String excerpt(String output) {
        if (output == null) {
            return "";
        }
        final String trimmed = output.trim();
        return trimmed.length() <= MAX_EXCERPT ? trimmed : trimmed.substring(0, MAX_EXCERPT) + "...";
    }
}
