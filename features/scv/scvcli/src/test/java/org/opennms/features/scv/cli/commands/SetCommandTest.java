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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineParser;

public class SetCommandTest {

    private final InputStream originalStdin = System.in;

    @After
    public void restoreStdin() {
        System.setIn(originalStdin);
    }

    private static void setStdin(final String content) {
        System.setIn(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }

    /** With --from-stdin and no positional password, the password is read from stdin. */
    @Test
    public void readsPasswordFromStdinWhenRequested() throws Exception {
        final SetCommand command = new SetCommand();
        new CmdLineParser(command).parseArgument("my-alias", "my-user", "--from-stdin");

        setStdin("s3cr3t-from-stdin\n");

        assertEquals("s3cr3t-from-stdin", command.resolvePassword());
    }

    /** A trailing newline from e.g. `echo` is stripped; the rest of the line is preserved verbatim. */
    @Test
    public void preservesPasswordContentFromStdin() throws Exception {
        final SetCommand command = new SetCommand();
        new CmdLineParser(command).parseArgument("my-alias", "my-user", "-S");

        setStdin("pass with spaces & symbols!@#\n");

        assertEquals("pass with spaces & symbols!@#", command.resolvePassword());
    }

    /** An explicit positional password wins over --from-stdin and stdin is not consulted. */
    @Test
    public void positionalPasswordTakesPrecedenceOverStdin() throws Exception {
        final SetCommand command = new SetCommand();
        new CmdLineParser(command).parseArgument("my-alias", "my-user", "explicit-pass", "--from-stdin");

        // Provide different content on stdin to prove it is ignored.
        setStdin("should-be-ignored\n");

        assertEquals("explicit-pass", command.resolvePassword());
    }
}
