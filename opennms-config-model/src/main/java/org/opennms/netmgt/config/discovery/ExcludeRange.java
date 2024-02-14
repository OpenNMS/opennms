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

import com.google.common.base.MoreObjects;

@XmlRootElement(name = "exclude-range")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("discovery-configuration.xsd")
public class ExcludeRange implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Starting address of the range.
     */
    @XmlElement(name = "begin", required = true)
    private String begin;

    /**
     * Ending address of the range.
     */
    @XmlElement(name = "end", required = true)
    private String end;

    /**
     * The monitoring location where this exclude range
     *  will be excluded
     */
    @XmlAttribute(name = "location")
    private String location;


    public ExcludeRange() {
    }

    public ExcludeRange(final String begin, final String end) {
        setBegin(begin);
        setEnd(end);
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

    public Optional<String> getLocation() {
        return Optional.ofNullable(location);
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            begin, 
                            end,
                            location);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof ExcludeRange) {
            final ExcludeRange temp = (ExcludeRange)obj;
            return Objects.equals(temp.begin, begin)
                    && Objects.equals(temp.end, end)
                    && Objects.equals(temp.location, location);
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("begin", begin)
                .add("end", end)
                .add("location", location)
                .toString();
    }
}