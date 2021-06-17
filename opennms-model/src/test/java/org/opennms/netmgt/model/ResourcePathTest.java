/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
