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


import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class DeviceConfigUtil {
    

    public static byte[] decompressGzipToBytes(byte[] source) throws IOException {
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(source))) {
            return ByteStreams.toByteArray(gis);
        }
    }

    public static boolean isGzipFile(String fileName) {
        return !Strings.isNullOrEmpty(fileName) && fileName.endsWith(".gz");
    }

}
