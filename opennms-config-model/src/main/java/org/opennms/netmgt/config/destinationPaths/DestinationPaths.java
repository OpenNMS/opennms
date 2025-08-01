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
