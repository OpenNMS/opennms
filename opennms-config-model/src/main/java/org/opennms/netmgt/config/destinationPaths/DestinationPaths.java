/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.destinationPaths;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name = "destinationPaths")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("destinationPaths.xsd")
public class DestinationPaths implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Header containing information about this configuration
     *  file.
     */
    @XmlElement(name = "header", required = true)
    private Header m_header;

    @XmlElement(name = "path", required = true)
    private List<Path> m_paths = new ArrayList<>();

    public DestinationPaths() {
    }

    public Header getHeader() {
        return m_header;
    }

    public void setHeader(final Header header) {
        m_header = header;
    }

    public List<Path> getPaths() {
        return m_paths;
    }

    public void setPaths(final List<Path> paths) {
        if (paths == m_paths) return;
        m_paths.clear();
        if (paths != null) m_paths.addAll(paths);
    }

    public void addPath(final Path path) {
        m_paths.add(path);
    }

    public boolean removePath(final Path path) {
        return m_paths.remove(path);
    }

    public void clearPaths() {
        m_paths.clear();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            m_header, 
                            m_paths);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof DestinationPaths) {
            final DestinationPaths temp = (DestinationPaths)obj;
            return Objects.equals(temp.m_header, m_header)
                    && Objects.equals(temp.m_paths, m_paths);
        }
        return false;
    }

    @Override
    public String toString() {
        return "DestinationPaths [header=" + m_header + ", paths="
                + m_paths + "]";
    }

}
