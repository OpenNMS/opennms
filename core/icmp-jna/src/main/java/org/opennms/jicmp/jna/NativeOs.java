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
package org.opennms.jicmp.jna;

import java.util.Locale;

/**
 * Operating system detection for the native socket implementations,
 * equivalent to the checks previously done with {@code com.sun.jna.Platform}.
 */
public final class NativeOs {
    private static final String OS_NAME = System.getProperty("os.name", "");
    private static final String OS = OS_NAME.toLowerCase(Locale.ROOT);

    private NativeOs() {}

    public static boolean isLinux() {
        return OS.startsWith("linux") || OS.startsWith("gnu/linux");
    }

    public static boolean isMac() {
        return OS.startsWith("mac") || OS.startsWith("darwin");
    }

    public static boolean isWindows() {
        return OS.startsWith("windows");
    }

    public static boolean isFreeBSD() {
        return OS.startsWith("freebsd") || OS.startsWith("gnu/kfreebsd");
    }

    public static boolean isOpenBSD() {
        return OS.startsWith("openbsd");
    }

    public static boolean isSolaris() {
        return OS.startsWith("solaris") || OS.startsWith("sunos");
    }

    /** BSD-style {@code sockaddr} layout (leading length byte): macOS and the BSDs. */
    public static boolean isBsdFamily() {
        return isMac() || isFreeBSD() || isOpenBSD();
    }

    public static String name() {
        return OS_NAME;
    }
}
