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
 * A file URL holding specific addresses to be polled. Each
 *  line in the URL file can be one of:
 *  "<IP><space>#<comments>", "<IP>", or
 *  "#<comments>". Lines starting with a '#' are ignored and so are
 *  characters after a '<space>#' in a line.
 */
@XmlRootElement(name = "include-url")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("discovery-configuration.xsd")
public class IncludeUrl implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * inner value
     */
    @XmlValue
    private String url;

    /**
     * The monitoring location where this include URL
     *  will be executed.
     */
    @XmlAttribute(name = "location")
    private String location;

    /**
     * The number of times a ping is retried for
     *  addresses listed in this file. If there is no response after the
     *  first ping to an address, it is tried again for the specified
     *  number of retries. This retry count overrides the
     *  default.
     */
    @XmlAttribute(name = "retries")
    private Integer retries;

    /**
     * The timeout on each poll for addresses listed in
     *  this file. This timeout overrides the default.
     */
    @XmlAttribute(name = "timeout")
    private Long timeout;

    @XmlAttribute(name = "foreign-source")
    private String foreignSource;

    public IncludeUrl() {
    }

    public IncludeUrl(final String url) {
        this.url = url;
    }

    public Optional<String> getUrl() {
        return Optional.ofNullable(url);
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

    public Optional<Integer> getRetries() {
        return Optional.ofNullable(retries);
    }

    public void setRetries(final Integer retries) {
        this.retries = retries;
    }

    public Optional<Long> getTimeout() {
        return Optional.ofNullable(timeout);
    }

    public void setTimeout(final Long timeout) {
        if (timeout != null && timeout == 0) {
            throw new IllegalArgumentException("Can't have a 0 timeout!");
        }
        this.timeout = timeout;
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
                            retries, 
                            timeout, 
                            foreignSource);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof IncludeUrl) {
            final IncludeUrl temp = (IncludeUrl)obj;
            return Objects.equals(temp.url, url)
                    && Objects.equals(temp.location, location)
                    && Objects.equals(temp.retries, retries)
                    && Objects.equals(temp.timeout, timeout)
                    && Objects.equals(temp.foreignSource, foreignSource);
        }
        return false;
    }

    @Override
    public String toString() {
        return "IncludeUrl [value=" + url + ", location="
                + location + ", retries=" + retries + ", timeout="
                + timeout + ", foreignSource=" + foreignSource + "]";
    }
}