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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;
import org.opennms.netmgt.model.ResourcePath;

public class TimeseriesUtilsTest {

    @Test
    public void shouldAddIndices() {
        test("a", "_idx0=(a,1)");
        test("a/b", "_idx0=(a,2)", "_idx1=(a:b,2)", "_idx2w=(a:b,*)");
        test("a/b/c", "_idx0=(a,3)", "_idx1=(a:b,3)", "_idx2w=(a:b,*)", "_idx2=(a:b:c,3)");
        test("a/b/c/d", "_idx0=(a,4)", "_idx1=(a:b,4)", "_idx2w=(a:b,*)", "_idx2=(a:b:c,4)", "_idx3=(a:b:c:d,4)");
    }

    private void test(final String path, String...expectedIndices) {
        Map<String, String> attributes = new HashMap<>();
        TimeseriesUtils.addIndicesToAttributes(ResourcePath.fromString(path), attributes);
        List<String> result = attributes.entrySet().stream()
                .map(e -> (e.getKey() + "=" + e.getValue()))
                .sorted()
                .collect(Collectors.toList());
        List<String> expectedResults = Arrays.asList(expectedIndices);
        expectedResults.sort(String::compareTo);
        assertEquals(expectedResults, result);
    }

}
