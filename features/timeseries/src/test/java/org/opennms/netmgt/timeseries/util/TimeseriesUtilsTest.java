/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.opennms.integration.api.v1.timeseries.Tag;
import org.opennms.netmgt.model.ResourcePath;

import com.google.re2j.Pattern;

public class TimeseriesUtilsTest {

    @Test
    public void regexShouldWork() {
        regexShouldWork("aa/bb/cc", "aa/bb/cc", 0, true);
        regexShouldWork("aa/bb/cc", "aa/bb/cc", 1, false);
        regexShouldWork("aa/bb/cc/dd", "aa/bb/cc", 0, false);
        regexShouldWork("aa/bb/cc", "aa/bb/cc/dd", 1, true);
        regexShouldWork("aa/bb/cc", "aa/bb/cc/dd/ee", 1, false);
        regexShouldWork("aa/bb/cc", "aa/bb/cc/dd/ee", 2, true);
    }

    private void regexShouldWork(String path, String testString, int depth, boolean expectToMatch) {
        assertEquals(expectToMatch,
                Pattern.matches(TimeseriesUtils.toSearchRegex(ResourcePath.fromString(path), depth), testString));
    }

    @Test
    public void toResourcePathShouldWork() {
        assertEquals(ResourcePath.fromString("aa/bb"), TimeseriesUtils.toResourcePath("aa/bb/cc")); // last element is treated as the name and not part of path
        assertEquals(ResourcePath.fromString(""), TimeseriesUtils.toResourcePath("aa")); // last element is treated as the name and not part of path
        assertEquals(ResourcePath.fromString(""), TimeseriesUtils.toResourcePath("")); // last element is treated as the name and not part of path
    }
}
