/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.discovery;


import org.opennms.netmgt.config.utils.ConfigUtils;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * A file URL holding specific addresses to be excluded. Each
 *  line in the URL file can be one of:
 *  "<IP><space>#<comments>", "<IP>", or
 *  "#<comments>". Lines starting with a '#' are ignored and so are
 *  characters after a '<space>#' in a line.
 */
public class ExcludeUrl implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * inner value
     */
    private String url;

    /**
     * The monitoring location where this include URL
     *  will be executed.
     */
    private String location;

    private String foreignSource;

    public ExcludeUrl() {
    }

    public ExcludeUrl(final String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = ConfigUtils.assertNotEmpty(url, "URL");
    }

    public Optional<String> getLocation() {
        return Optional.ofNullable(location);
    }

    public void setLocation(final String location) {
        this.location = ConfigUtils.normalizeString(location);
    }

    public Optional<String> getForeignSource() {
        return Optional.ofNullable(foreignSource);
    }

    public void setForeignSource(final String foreignSource) {
        this.foreignSource = ConfigUtils.normalizeString(foreignSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            url,
                            location,
                            foreignSource);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof ExcludeUrl) {
            final ExcludeUrl temp = (ExcludeUrl)obj;
            return Objects.equals(temp.url, url)
                    && Objects.equals(temp.location, location)
                    && Objects.equals(temp.foreignSource, foreignSource);
        }
        return false;
    }

    @Override
    public String toString() {
        return "ExcludeUrl [value=" + url + ", location="
                + location + ", foreignSource=" + foreignSource + "]";
    }
}
