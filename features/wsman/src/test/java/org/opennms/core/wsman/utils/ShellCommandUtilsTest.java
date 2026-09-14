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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.regex.PatternSyntaxException;

import org.junit.Test;
import org.opennms.core.wsman.shell.CommandResult;
import org.opennms.core.wsman.shell.ShellOptions;

public class ShellCommandUtilsTest {

    @Test
    public void passesArgumentsThroughVerbatim() {
        assertArrayEquals(new String[0], ShellCommandUtils.toArguments(null));
        assertArrayEquals(new String[0], ShellCommandUtils.toArguments("   "));
        assertArrayEquals(new String[] {"/all"}, ShellCommandUtils.toArguments("/all"));
        assertArrayEquals(new String[] {"query   w32time"}, ShellCommandUtils.toArguments("  query   w32time "));
        // Quotes and backslashes are left for the Windows host to interpret
        assertArrayEquals(new String[] {"\"C:\\Program Files\" /s"}, ShellCommandUtils.toArguments("\"C:\\Program Files\" /s"));
        assertArrayEquals(new String[] {"-Command \"Write-Host 'hi there'\""},
                ShellCommandUtils.toArguments("-Command \"Write-Host 'hi there'\""));
    }

    @Test
    public void matchesBanners() {
        final String output = "SERVICE_NAME: w32time\r\n        STATE              : 4  RUNNING\r\n";
        // Anything
        assertTrue(ShellCommandUtils.bannerMatches(null, output));
        assertTrue(ShellCommandUtils.bannerMatches("", output));
        assertTrue(ShellCommandUtils.bannerMatches("*", output));
        assertTrue(ShellCommandUtils.bannerMatches("*", null));
        // Substring
        assertTrue(ShellCommandUtils.bannerMatches("RUNNING", output));
        assertFalse(ShellCommandUtils.bannerMatches("STOPPED", output));
        assertFalse(ShellCommandUtils.bannerMatches("RUNNING", null));
        // Regex, whole output, dot spans lines
        assertTrue(ShellCommandUtils.bannerMatches("~.*STATE\\s*:\\s*4\\s+RUNNING.*", output));
        assertFalse(ShellCommandUtils.bannerMatches("~.*STOPPED.*", output));
        // Regex is anchored to the whole output
        assertFalse(ShellCommandUtils.bannerMatches("~RUNNING", output));
    }

    @Test(expected = PatternSyntaxException.class)
    public void rejectsInvalidRegexBanner() {
        ShellCommandUtils.bannerMatches("~(unclosed", "x");
    }

    @Test
    public void matchesExitCodes() {
        assertTrue(ShellCommandUtils.exitCodeMatches(null, 3));
        assertTrue(ShellCommandUtils.exitCodeMatches("*", 3));
        assertTrue(ShellCommandUtils.exitCodeMatches("0", 0));
        assertTrue(ShellCommandUtils.exitCodeMatches(" 1 ", 1));
        assertFalse(ShellCommandUtils.exitCodeMatches("0", 1));
    }

    @Test
    public void checksResults() {
        assertEquals(Optional.empty(), ShellCommandUtils.checkResult(new CommandResult(0, "ok", ""), "0", "ok"));
        assertEquals(Optional.empty(), ShellCommandUtils.checkResult(new CommandResult(7, "ok", ""), "*", null));

        final Optional<String> badExit = ShellCommandUtils.checkResult(new CommandResult(1, "", "boom"), "0", null);
        assertTrue(badExit.isPresent());
        assertTrue(badExit.get(), badExit.get().contains("exited with code 1"));
        assertTrue(badExit.get(), badExit.get().contains("boom"));

        final Optional<String> badBanner = ShellCommandUtils.checkResult(new CommandResult(0, "STOPPED", ""), "0", "RUNNING");
        assertTrue(badBanner.isPresent());
        assertTrue(badBanner.get(), badBanner.get().contains("RUNNING"));
        assertTrue(badBanner.get(), badBanner.get().contains("STOPPED"));
    }

    @Test
    public void buildsShellOptions() {
        ShellOptions defaults = ShellCommandUtils.buildShellOptions(true, null, null);
        assertTrue(defaults.isNoProfile());
        assertEquals(65001, defaults.getCodepage());
        assertEquals(null, defaults.getWorkingDirectory());

        ShellOptions custom = ShellCommandUtils.buildShellOptions(false, 437, "C:\\Temp");
        assertFalse(custom.isNoProfile());
        assertEquals(437, custom.getCodepage());
        assertEquals("C:\\Temp", custom.getWorkingDirectory());
    }

    @Test
    public void truncatesExcerpts() {
        assertEquals("", ShellCommandUtils.excerpt(null));
        assertEquals("abc", ShellCommandUtils.excerpt("  abc \r\n"));
        String excerpt = ShellCommandUtils.excerpt("x".repeat(ShellCommandUtils.MAX_EXCERPT + 10));
        assertEquals(ShellCommandUtils.MAX_EXCERPT + 3, excerpt.length());
        assertTrue(excerpt.endsWith("..."));
    }
}
