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
package org.opennms.netmgt.telemetry.protocols.openconfig.adapter;

import static org.junit.Assert.assertFalse;

import java.nio.file.Files;
import java.util.regex.Pattern;

import org.junit.Test;

public class GnmiMappingScriptTest {

    @Test
    public void scriptHasNoMavenFilterTokens() throws Exception {
        String text = Files.readString(GnmiMappingScript.file().toPath());
        assertFalse("the script must not contain a dollar sign", text.contains("$"));
        assertFalse("the script must not contain an @word@ token", Pattern.compile("@[A-Za-z0-9._-]+@").matcher(text).find());
    }
}
