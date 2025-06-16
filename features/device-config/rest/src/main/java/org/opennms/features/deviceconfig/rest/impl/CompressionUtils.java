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

import com.google.common.base.Strings;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class CompressionUtils {
    public static byte[] tarGzipMultipleFiles(Map<String, byte[]> fileNameToDataMap) throws IOException {
        if (fileNameToDataMap == null || fileNameToDataMap.isEmpty()) {
            return null;
        }

        byte[] tarBytes = tarMultipleFiles(fileNameToDataMap);
        var byteOutputStream = new ByteArrayOutputStream();

        try (var gzipOut = new GzipCompressorOutputStream(byteOutputStream)) {
            gzipOut.write(tarBytes);
        }

        return byteOutputStream.toByteArray();
    }

    public static Map<String, byte[]> unTarGzipMultipleFiles(byte[] tarGzipData) throws IOException {
        if (tarGzipData == null) {
            return new HashMap<>();
        }

        var result = new HashMap<String, byte[]>();
        var byteArrayInputStream = new ByteArrayInputStream(tarGzipData);
        var gzipInputStream = new GZIPInputStream(byteArrayInputStream);

        try (var tarInputStream = new TarArchiveInputStream(gzipInputStream)) {
            TarArchiveEntry tarEntry = null;
            final int BUFLEN = 1024;
            byte[] buffer = new byte[BUFLEN];

            while ((tarEntry = tarInputStream.getNextTarEntry()) != null) {
                if (tarEntry.isFile()) {
                    String fileName = tarEntry.getName();

                    var byteOutput = new ByteArrayOutputStream();

                    try (var bufOut = new BufferedOutputStream(byteOutput)) {
                        int len = 0;

                        while ((len = tarInputStream.read(buffer)) != -1) {
                            bufOut.write(buffer, 0, len);
                        }
                    }

                    byte[] bytesRead = byteOutput.toByteArray();
                    result.put(fileName, bytesRead);
                }
            }
        }

        return result;
    }

    public static byte[] tarMultipleFiles(Map<String, byte[]> fileNameToDataMap) throws IOException {
        var byteOutputStream = new ByteArrayOutputStream();

        try (var tarOut = new TarArchiveOutputStream(byteOutputStream)) {
            for (Map.Entry<String, byte[]> entry : fileNameToDataMap.entrySet()) {
                if (!Strings.isNullOrEmpty(entry.getKey()) && entry.getValue() != null) {
                    var archiveEntry = new TarArchiveEntry(entry.getKey());
                    archiveEntry.setName(entry.getKey());
                    archiveEntry.setSize(entry.getValue().length);

                    tarOut.putArchiveEntry(archiveEntry);
                    tarOut.write(entry.getValue());
                    tarOut.closeArchiveEntry();
                }
            }
        }

        return byteOutputStream.toByteArray();
    }
}
