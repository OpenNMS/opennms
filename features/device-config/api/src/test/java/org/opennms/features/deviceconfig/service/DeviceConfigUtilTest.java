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
package org.opennms.features.deviceconfig.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class DeviceConfigUtilTest {

    private static byte[] utf8(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    // -------------------------------------------------------------------------
    // stripComments
    // -------------------------------------------------------------------------

    @Test
    public void stripsLineCommentSlashSlash() {
        assertEquals("hostname router \n", DeviceConfigUtil.stripComments("hostname router // generated\n"));
    }

    @Test
    public void stripsLineCommentHash() {
        assertEquals("set system host-name router \n", DeviceConfigUtil.stripComments("set system host-name router # generated\n"));
    }

    @Test
    public void stripsBlockComment() {
        assertEquals("interface eth0  {\n}\n", DeviceConfigUtil.stripComments("interface eth0 /* management */ {\n}\n"));
    }

    @Test
    public void stripsMultiLineBlockComment() {
        String input = "interface eth0 {\n/* last modified:\n   2024-01-01 */\n  address 10.0.0.1;\n}\n";
        String expected = "interface eth0 {\n  address 10.0.0.1;\n}\n";
        assertEquals(expected, DeviceConfigUtil.stripComments(input));
    }

    @Test
    public void dropsBlankLinesLeftByStripping() {
        // A line that consists entirely of a comment should disappear
        String input = "hostname router\n// auto-generated timestamp\ninterface eth0\n";
        String expected = "hostname router\ninterface eth0\n";
        assertEquals(expected, DeviceConfigUtil.stripComments(input));
    }

    // -------------------------------------------------------------------------
    // configsAreEqual
    // -------------------------------------------------------------------------

    @Test
    public void equalBytesAreEqual() {
        byte[] a = utf8("hostname router\n");
        assertTrue(DeviceConfigUtil.configsAreEqual(a, a, "UTF-8", false));
    }

    @Test
    public void differentBytesAreNotEqual() {
        assertFalse(DeviceConfigUtil.configsAreEqual(utf8("hostname router\n"), utf8("hostname switch\n"), "UTF-8", false));
    }

    @Test
    public void commentOnlyDiffIsEqualWhenIgnoring() {
        byte[] a = utf8("hostname router\n// timestamp: 2024-01-01\n");
        byte[] b = utf8("hostname router\n// timestamp: 2024-01-02\n");
        assertTrue(DeviceConfigUtil.configsAreEqual(a, b, "UTF-8", true));
    }

    @Test
    public void commentOnlyDiffIsNotEqualWhenNotIgnoring() {
        byte[] a = utf8("hostname router\n// timestamp: 2024-01-01\n");
        byte[] b = utf8("hostname router\n// timestamp: 2024-01-02\n");
        assertFalse(DeviceConfigUtil.configsAreEqual(a, b, "UTF-8", false));
    }

    @Test
    public void realChangeIsDetectedEvenWhenIgnoringComments() {
        byte[] a = utf8("hostname router\n// comment\n");
        byte[] b = utf8("hostname switch\n// comment\n");
        assertFalse(DeviceConfigUtil.configsAreEqual(a, b, "UTF-8", true));
    }

    @Test
    public void nullBothIsEqual() {
        assertTrue(DeviceConfigUtil.configsAreEqual(null, null, "UTF-8", false));
    }

    @Test
    public void nullOneIsNotEqual() {
        assertFalse(DeviceConfigUtil.configsAreEqual(utf8("x"), null, "UTF-8", false));
        assertFalse(DeviceConfigUtil.configsAreEqual(null, utf8("x"), "UTF-8", false));
    }
}
