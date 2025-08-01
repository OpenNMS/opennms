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
package org.opennms.features.apilayer.common;

import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.integration.api.v1.runtime.Version;

public class VersionBean implements Version {
    private static final Pattern versionPattern = Pattern.compile("^(\\d+).(\\d+)\\.(\\d+)(?<snapshot>.+)?$");

    private final int major;
    private final int minor;
    private final int patch;
    private final boolean isSnapshot;

    public VersionBean(String version) {
        final Matcher m = versionPattern.matcher(version);
        if (m.matches()) {
            major = Integer.parseInt(m.group(1));
            minor = Integer.parseInt(m.group(2));
            patch = Integer.parseInt(m.group(3));
            isSnapshot = m.group("snapshot") != null;
        } else {
            throw new IllegalArgumentException("Unsupported version string: " + version);
        }
    }

    @Override
    public int getMajor() {
        return major;
    }

    @Override
    public int getMinor() {
        return minor;
    }

    @Override
    public int getPatch() {
        return patch;
    }

    @Override
    public boolean isSnapshot() {
        return isSnapshot;
    }

    @Override
    public int compareTo(Version v) {
        if (v == null) {
            return -1;
        }
        return Comparator.comparingInt(Version::getMajor)
                .thenComparingInt(Version::getMinor)
                .thenComparingInt(Version::getPatch)
                .compare(this, v);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersionBean that = (VersionBean) o;
        return major == that.major &&
                minor == that.minor &&
                patch == that.patch &&
                isSnapshot == that.isSnapshot;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, isSnapshot);
    }

    @Override
    public String toString() {
        return String.format("v%d.%d.%d%s", major, minor, patch, isSnapshot ? "-SNAPSHOT" : "");
    }
}
