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
package org.opennms.features.deviceconfig.rest.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

public class CompressionUtilsTest {

    @Test
    public void gzipMultipleFilesNullTest() {
        byte[] result = null;

        try {
            result = CompressionUtils.tarGzipMultipleFiles(null);
        } catch (IOException e) {
            Assert.fail("IOException thrown when fileNameToDataMap argument was null");
        }

        Assert.assertNull(result);
    }

    @Test
    public void gzipMultipleFilesEmptyTest() {
        byte[] result = null;

        try {
            result = CompressionUtils.tarGzipMultipleFiles(new HashMap<String, byte[]>());
        } catch (IOException e) {
            Assert.fail("IOException thrown when fileNameToDataMap argument was empty");
        }

        Assert.assertNull(result);
    }

    @Test
    public void gzipMultipleFilesAndExtractTest() {
        var keys = List.of("one.cfg", "two.cfg", "three.cfg", "four.cfg");
        var values = List.of(new byte[] { 1 }, new byte[] { 2 }, new byte[] { 3 }, new byte[] { 4 });

        Map<String,byte[]> fileNameToDataMap =
            IntStream.range(0, keys.size()).boxed().collect(Collectors.toMap(keys::get, values::get));

        byte[] result = null;

        try {
            result = CompressionUtils.tarGzipMultipleFiles(fileNameToDataMap);
        } catch (IOException e) {
            Assert.fail("IOException thrown during gzipMultipleFiles");
        }

        Assert.assertNotNull(result);

        Map<String,byte[]> resultMap = null;

        try {
            resultMap = CompressionUtils.unTarGzipMultipleFiles(result);
        } catch (IOException e) {
            Assert.fail("IOException throw during untarGzipMultipleFiles");
        }

        final Map<String,byte[]> finalResultMap = resultMap;
        Assert.assertNotNull(finalResultMap);
        Assert.assertEquals(4, finalResultMap.size());
        Assert.assertTrue(keys.stream().allMatch(finalResultMap::containsKey));

        for (int index : IntStream.range(0, keys.size()).toArray()) {
            String key = keys.get(index);
            byte[] value = values.get(index);
            Assert.assertArrayEquals(finalResultMap.get(key), value);
        }
    }
}
