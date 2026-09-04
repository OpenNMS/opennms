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
package org.opennms.smoketest.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Shared utilities for Cortex TSS plugin smoke tests.
 */
public final class CortexTestUtils {

    private CortexTestUtils() {}

    /**
     * Resolve the Cortex TSS plugin KAR file path.
     *
     * <p>Set {@code -Dcortex.kar=/path/to/opennms-cortex-tss-plugin.kar} to provide the KAR explicitly.
     * If not set, returns {@code null} (the KAR must then be available via {@code -Dorg.opennms.dev.m2}).</p>
     *
     * @return the KAR file path, or {@code null} if not set
     * @throws IllegalStateException if the property is set but the file does not exist
     */
    public static Path resolveKarFile() {
        String karPath = System.getProperty("cortex.kar");
        if (karPath != null) {
            Path p = Paths.get(karPath);
            if (p.toFile().exists()) {
                return p;
            }
            throw new IllegalStateException("cortex.kar path does not exist: " + karPath);
        }
        return null;
    }
}
