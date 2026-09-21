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
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

public class DeviceConfigUtil {

    /**
     * Returns true if two config byte arrays are considered equal.
     * Comparison is done as text using the given encoding.
     * When {@code ignoreComments} is true, C-style comments ({@code //}, {@code #},
     * {@code /* ... *\/}) are stripped from both sides before comparing.
     */
    public static boolean configsAreEqual(byte[] a, byte[] b, String encoding, boolean ignoreComments) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        if (!ignoreComments) {
            return Arrays.equals(a, b);
        }
        try {
            Charset charset = Charset.forName(encoding);
            return stripComments(new String(a, charset)).equals(stripComments(new String(b, charset)));
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            return Arrays.equals(a, b);
        }
    }

    /**
     * Removes C-style comments from a configuration string:
     * <ul>
     *   <li>{@code /* ... *\/} block comments (including multi-line)</li>
     *   <li>{@code //} line comments</li>
     *   <li>{@code #} line comments</li>
     * </ul>
     * Lines that are empty or whitespace-only after removal are also dropped,
     * so a diff that only adds or removes comments is not treated as a change.
     */
    public static String stripComments(String content) {
        // block comments /* ... */ — (?s) makes . match newlines
        content = content.replaceAll("(?s)/\\*.*?\\*/", "");
        // // line comments
        content = content.replaceAll("//[^\r\n]*", "");
        // # line comments
        content = content.replaceAll("#[^\r\n]*", "");
        // drop lines that are now blank or whitespace-only
        content = content.replaceAll("(?m)^[ \t]*\r?\n", "");
        return content;
    }

    public static byte[] decompressGzipToBytes(byte[] source) throws IOException {
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(source))) {
            return ByteStreams.toByteArray(gis);
        }
    }

    public static boolean isGzipFile(String fileName) {
        return !Strings.isNullOrEmpty(fileName) && fileName.endsWith(".gz");
    }

}
