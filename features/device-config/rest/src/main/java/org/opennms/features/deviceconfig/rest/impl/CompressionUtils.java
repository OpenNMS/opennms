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
