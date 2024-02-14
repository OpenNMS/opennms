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
