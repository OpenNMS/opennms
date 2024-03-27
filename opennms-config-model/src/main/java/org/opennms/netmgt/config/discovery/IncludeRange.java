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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "include-range")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("discovery-configuration.xsd")
public class IncludeRange implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The monitoring location where this include range
     *  will be executed.
     */
    @XmlAttribute(name = "location")
    private String location;

    /**
     * The number of times a ping is retried for this
     *  address range. If there is no response after the first ping to an
     *  address, it is tried again for the specified number of retries. This
     *  retry count overrides the default.
     */
    @XmlAttribute(name = "retries")
    private Integer retries;

    /**
     * The timeout on each poll for this address range. This
     *  timeout overrides the default.
     */
    @XmlAttribute(name = "timeout")
    private Long timeout;

    @XmlAttribute(name = "foreign-source")
    private String foreignSource;

    /**
     * Starting address of the range.
     */
    @XmlElement(name = "begin", required = true)
    private String begin;

    /**
     * Ending address of the range. If the starting
     *  address is greater than the ending address, they are
     *  swapped.
     */
    @XmlElement(name = "end", required = true)
    private String end;

    public IncludeRange() {
    }

    public IncludeRange(final String begin, final String end) {
        setBegin(begin);
        setEnd(end);
    }

    public Optional<String> getLocation() {
        return Optional.ofNullable(location);
    }

    public void setLocation(final String location) {
        this.location = ConfigUtils.normalizeString(location);
    }

    public Optional<Integer> getRetries() {
        return Optional.ofNullable( retries);
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

    public String getBegin() {
        return begin;
    }

    public void setBegin(final String begin) {
        this.begin = ConfigUtils.assertNotEmpty(begin, "begin");
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(final String end) {
        this.end = ConfigUtils.assertNotEmpty(end, "end");
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            location, 
                            retries, 
                            timeout, 
                            foreignSource, 
                            begin, 
                            end);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof IncludeRange) {
            final IncludeRange temp = (IncludeRange)obj;
            return Objects.equals(temp.location, location)
                    && Objects.equals(temp.retries, retries)
                    && Objects.equals(temp.timeout, timeout)
                    && Objects.equals(temp.foreignSource, foreignSource)
                    && Objects.equals(temp.begin, begin)
                    && Objects.equals(temp.end, end);
        }
        return false;
    }

    @Override
    public String toString() {
        return "IncludeRange [location=" + location + ", retries="
                + retries + ", timeout=" + timeout
                + ", foreignSource=" + foreignSource + ", begin="
                + begin + ", end=" + end + "]";
    }

}
