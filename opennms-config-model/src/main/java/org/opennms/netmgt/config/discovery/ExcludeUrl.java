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
package org.opennms.netmgt.config.discovery;


import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * A file URL holding specific addresses to be excluded. Each
 *  line in the URL file can be one of:
 *  "<IP><space>#<comments>", "<IP>", or
 *  "#<comments>". Lines starting with a '#' are ignored and so are
 *  characters after a '<space>#' in a line.
 */
@XmlRootElement(name = "exclude-url")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("discovery-configuration.xsd")
public class ExcludeUrl implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * inner value
     */
    @XmlValue
    private String m_url;

    /**
     * The monitoring location where this include URL
     *  will be executed.
     */
    @XmlAttribute(name = "location")
    private String m_location;

    @XmlAttribute(name = "foreign-source")
    private String m_foreignSource;

    public ExcludeUrl() {
    }

    public ExcludeUrl(final String url) {
        m_url = url;
    }

    public String getUrl() {
        return m_url;
    }

    public void setUrl(final String url) {
        m_url = ConfigUtils.assertNotEmpty(url, "URL");
    }

    public Optional<String> getLocation() {
        return Optional.ofNullable(m_location);
    }

    public void setLocation(final String location) {
        m_location = ConfigUtils.normalizeString(location);
    }  

    public Optional<String> getForeignSource() {
        return Optional.ofNullable(m_foreignSource);
    }

    public void setForeignSource(final String foreignSource) {
        m_foreignSource = ConfigUtils.normalizeString(foreignSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            m_url, 
                            m_location, 
                            m_foreignSource);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof ExcludeUrl) {
            final ExcludeUrl temp = (ExcludeUrl)obj;
            return Objects.equals(temp.m_url, m_url)
                    && Objects.equals(temp.m_location, m_location)
                    && Objects.equals(temp.m_foreignSource, m_foreignSource);
        }
        return false;
    }

    @Override
    public String toString() {
        return "ExcludeUrl [value=" + m_url + ", location="
                + m_location + ", foreignSource=" + m_foreignSource + "]";
    }

}
