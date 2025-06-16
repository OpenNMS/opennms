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
package org.opennms.netmgt.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

public class ResourcePathTest {

    @Test
    public void canSanitizePath() {
        assertEquals("abc123", ResourcePath.sanitize("abc123"));
        assertEquals("My_Path_", ResourcePath.sanitize("My Path!"));
        assertEquals("_________", ResourcePath.sanitize("¯\\_(ツ)_/¯"));
    }

    @Test
    public void verifyThatChildDepthIsCalculatedCorrectly() {
        ResourcePath parent = ResourcePath.fromString("aa/bb/cc/dd");
        assertEquals(0, parent.relativeDepth(ResourcePath.fromString("aa/bb/cc/dd")));
        assertEquals(1, parent.relativeDepth(ResourcePath.fromString("aa/bb/cc/dd/ee")));
        assertEquals(-1, parent.relativeDepth(ResourcePath.fromString("aa/xx/cc")));
    }

    @Test
    public void pathElementsMustNotContainForwardSlash() {
        new ResourcePath("aa", "bb"); // this should work
        assertThrowsException(IllegalArgumentException.class, () -> new ResourcePath("aa", "b/b"));
        assertThrowsException(IllegalArgumentException.class, () -> new ResourcePath(ResourcePath.fromString("aa"), "b/b"));
        assertThrowsException(IllegalArgumentException.class, () -> new ResourcePath(Arrays.asList("aa", "b/b")));
        assertThrowsException(IllegalArgumentException.class, () -> new ResourcePath(Arrays.asList("aa", "b/b")));
    }

    public static void assertThrowsException(Class<? extends Throwable> expectedException, Runnable function) {
        try {
            function.run();
        } catch(Exception e) {
            if(!expectedException.isAssignableFrom(e.getClass())) {
                fail(String.format("Expected exception: %s but was %s", expectedException.getName(), e.getClass().getName()));
            }
            return;
        }
        fail(String.format("Expected exception: %s but none was thrown.", expectedException.getName()));
    }
}
