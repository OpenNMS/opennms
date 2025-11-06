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
package org.opennms.features.vaadin.utils;

import java.io.File;

public final class FileValidationUtils {
    public static  boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        // Block path traversal sequences
        if (fileName.contains("..") ||
                fileName.contains("/") ||
                fileName.contains("\\") ||
                fileName.contains("\0") || // null byte
                fileName.contains("%00") || // encoded null byte
                fileName.contains("%2e") || // encoded dot
                fileName.contains("%2f") || // encoded slash
                fileName.contains("%5c")) { // encoded backslash
            return false;
        }

        // Block other dangerous patterns
        if (fileName.matches(".*[<>:\"|?*].*")) {
            return false; // Windows reserved characters
        }

        // Extract just the filename part (in case any path components slipped through)
        String nameOnly = new File(fileName).getName();
        if (!nameOnly.equals(fileName)) {
            return false; // Path components detected
        }

        // Allow only safe characters for filenames
        if (!nameOnly.matches("^[a-zA-Z0-9][a-zA-Z0-9._\\-]+$")) {
            return false;
        }

        // Prevent reserved filenames (Windows)
        String nameWithoutExt = nameOnly.replaceFirst("\\.[^.]+$", "").toUpperCase();
        if (nameWithoutExt.matches("^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])$")) {
            return false;
        }
        return true;
    }

    public static boolean isFileNameTooLong(String fileName) {
        return fileName != null && fileName.length() > 255;
    }

}

