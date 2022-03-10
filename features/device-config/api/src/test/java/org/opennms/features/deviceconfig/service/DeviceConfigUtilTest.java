/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.features.deviceconfig.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

public class DeviceConfigUtilTest {

    @Test
    public void gzipMultipleFilesNullTest() {
        byte[] result = null;

        try {
            result = DeviceConfigUtil.tarGzipMultipleFiles(null);
        } catch (IOException e) {
            Assert.fail("IOException thrown when fileNameToDataMap argument was null");
        }

        Assert.assertNull(result);
    }

    @Test
    public void gzipMultipleFilesEmptyTest() {
        byte[] result = null;

        try {
            result = DeviceConfigUtil.tarGzipMultipleFiles(new HashMap<String, byte[]>());
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
            result = DeviceConfigUtil.tarGzipMultipleFiles(fileNameToDataMap);
        } catch (IOException e) {
            Assert.fail("IOException thrown during gzipMultipleFiles");
        }

        Assert.assertNotNull(result);

        Map<String,byte[]> resultMap = null;

        try {
            resultMap = DeviceConfigUtil.unTarGzipMultipleFiles(result);
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
