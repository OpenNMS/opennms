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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.opennms.features.deviceconfig.sshscripting.impl.SshScriptingServiceImpl.extractCapturedBytes;
import static org.opennms.features.deviceconfig.sshscripting.impl.SshScriptingServiceImpl.matchAndConsume;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class SshScriptingServiceImplTest {

    // =========================================================================
    // extractCapturedBytes — unit tests for awaitAndCapture capture semantics
    // =========================================================================

    @Test
    public void extractCapturedBytesReturnsFullTailWhenNoCaptureEnd() {
        // captureEnd == -1: no await fired after capture, return everything from captureStart
        var allBytes = "config data\n".getBytes(StandardCharsets.UTF_8);
        var result = extractCapturedBytes(allBytes, 0, -1);
        assertThat(new String(result, StandardCharsets.UTF_8), is("config data\n"));
    }

    @Test
    public void extractCapturedBytesExcludesPromptLineWhenCaptureEndAtLineStart() {
        // captureEnd points right to the start of the prompt line (just after the preceding \n)
        // allBytes = "config data\nrouter#\n" — captureEnd=12 is the 'r' of "router#"
        var allBytes = "config data\nrouter#\n".getBytes(StandardCharsets.UTF_8);
        // allBytes[11]='\n', allBytes[12]='r' — captureEnd=12 → scan stops immediately at allBytes[11]
        var result = extractCapturedBytes(allBytes, 0, 12);
        assertThat(new String(result, StandardCharsets.UTF_8), is("config data\n"));
    }

    @Test
    public void extractCapturedBytesScanBackToNewlineWhenCaptureEndMidLine() {
        // captureEnd points mid-prompt-line (e.g. await: # on "router#");
        // the backwards scan must remove the entire "router" prefix too
        var allBytes = "config data\nrouter#\n".getBytes(StandardCharsets.UTF_8);
        // allBytes: ...a(10)\n(11)r(12)o(13)u(14)t(15)e(16)r(17)#(18)\n(19)
        // captureEnd=18 ('r' before '#') → scan: 17='r',16='e',...,12='r',11='\n' → end=12
        var result = extractCapturedBytes(allBytes, 0, 18);
        assertThat(new String(result, StandardCharsets.UTF_8), is("config data\n"));
    }

    @Test
    public void extractCapturedBytesIsEmptyWhenCaptureStartEqualsEnd() {
        // captureStart == captureEnd: nothing arrived between capture and the prompt
        var allBytes = "router#\n".getBytes(StandardCharsets.UTF_8);
        var result = extractCapturedBytes(allBytes, 0, 0);
        assertThat(result.length, is(0));
    }

    // =========================================================================

    @Test
    public void testMatchAndConsume() throws Exception {
        var bytes = new byte[] { 0, 1, 2, 3 };
        {
            var bos = new ByteArrayOutputStream();
            bos.write(bytes);
            assertThat(matchAndConsume(bos, bytes), is(true));
            assertThat(bos.size(), is(0));
        }
        {
            var bos = new ByteArrayOutputStream();
            bos.write(bytes);
            assertThat(matchAndConsume(bos, new byte[] { 0, 1 }), is(true));
            assertThat(bos.size(), is(2));
            assertThat(bos.toByteArray()[0], is((byte)2));
            assertThat(bos.toByteArray()[1], is((byte)3));
        }
        {
            var bos = new ByteArrayOutputStream();
            bos.write(bytes);
            assertThat(matchAndConsume(bos, new byte[] { 1, 2 }), is(true));
            assertThat(bos.size(), is(1));
            assertThat(bos.toByteArray()[0], is((byte)3));
        }
        {
            var bos = new ByteArrayOutputStream();
            bos.write(bytes);
            assertThat(matchAndConsume(bos, new byte[] { 2, 3 }), is(true));
            assertThat(bos.size(), is(0));
        }
        {
            var bos = new ByteArrayOutputStream();
            bos.write(new byte[] { 1, 1, 1, 1, 1 });
            assertThat(matchAndConsume(bos, new byte[] { 1, 1 }), is(true));
            assertThat(bos.size(), is(3));
            assertThat(matchAndConsume(bos, new byte[] { 1, 1 }), is(true));
            assertThat(bos.size(), is(1));
            assertThat(matchAndConsume(bos, new byte[] { 1, 1 }), is(false));
        }
    }
}
