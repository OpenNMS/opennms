/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.plugins.elasticsearch.rest.index;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class StringCollapserTest {

    @Test
    public void shouldCollapseAllStringsWhenCollapsingAtBeginningIsTrue() {
        List<String> result = StringCollapser
                .forList(Arrays.asList("aa1", "aa2","aa3","aa4","xx1"))
                .collapseAfterChars(2)
                .doCollapsingAtBeginning(true)
                .collapse();
        assertEquals("aa*", result.get(0));
        assertEquals("xx*", result.get(1));
    }

    @Test
    public void shouldCollapseNoStringsAtBeginningIfCollapsingAtBeginningIsFalse() {
        List<String> result = StringCollapser
                .forList(Arrays.asList("aa1", "aa2","aa3","aa4","xx1"))
                .collapseAfterChars(2)
                .doCollapsingAtBeginning(false)
                .collapse();
        assertEquals(5, result.size());
        assertEquals("aa1", result.get(0));
        assertEquals("xx*", result.get(4));

    }

    @Test
    public void shouldCollapseNoStringsAtEndIfCollapsingAtEndIsFalse() {
        List<String> result = StringCollapser
                .forList(Arrays.asList("aa1", "aa2","aa3","aa4","xx1", "xx2"))
                .collapseAfterChars(2)
                .doCollapsingAtBeginning(true)
                .doCollapsingAtEnd(false)
                .collapse();
        assertEquals("aa*", result.get(0));
        assertEquals("xx1", result.get(1));
        assertEquals("xx2", result.get(2));
        assertEquals(3, result.size());
    }
    @Test
    public void shouldCollapseNoStringsAtEitherEnd() {
        List<String> result = StringCollapser
                .forList(Arrays.asList("aa1", "aa2","bb1","bb2","xx1", "xx2"))
                .collapseAfterChars(2)
                .doCollapsingAtBeginning(false)
                .doCollapsingAtEnd(false)
                .collapse();

        assertEquals("aa1", result.get(0));
        assertEquals("aa2", result.get(1));
        assertEquals("bb*", result.get(2));
        assertEquals("xx1", result.get(3));
        assertEquals("xx2", result.get(4));
        assertEquals(5, result.size());
    }
}