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
package org.opennms.core.network;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class IpListFromUrlTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testInlineComments() throws Exception {
        final File file = tempFolder.newFile("include.txt");
        Files.write(file.toPath(), Arrays.asList(
                "# full-line comment",
                "",
                "10.0.0.1",
                "10.0.0.2 # one space",
                "10.0.0.3   # several spaces",
                "10.0.0.4\t# tab",
                "10.0.0.5# no space",
                "  fe80::1  # indented"), StandardCharsets.UTF_8);

        assertEquals(Arrays.asList("10.0.0.1", "10.0.0.2", "10.0.0.3", "10.0.0.4", "10.0.0.5", "fe80::1"),
                IpListFromUrl.fetch(file.toURI().toURL().toString()));
    }
}
