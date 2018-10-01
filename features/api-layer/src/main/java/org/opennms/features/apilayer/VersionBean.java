/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer;

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
