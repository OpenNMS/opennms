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
package org.opennms.netmgt.config.threshd;


import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Range of adresses to be included in this
 *  package
 */
@XmlRootElement(name = "include-range")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("thresholding.xsd")
public class IncludeRange implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Starting address of the range
     */
    @XmlAttribute(name = "begin", required = true)
    private String m_begin;

    /**
     * Ending address of the range
     */
    @XmlAttribute(name = "end", required = true)
    private String m_end;

    public IncludeRange() {
    }

    public String getBegin() {
        return m_begin;
    }

    public void setBegin(final String begin) {
        m_begin = ConfigUtils.assertNotEmpty(begin, "begin");
    }

    public String getEnd() {
        return m_end;
    }

    public void setEnd(final String end) {
        m_end = ConfigUtils.assertNotEmpty(end, "end");
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_begin, m_end);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof IncludeRange) {
            final IncludeRange that = (IncludeRange)obj;
            return Objects.equals(this.m_begin, that.m_begin)
                    && Objects.equals(this.m_end, that.m_end);
        }
        return false;
    }

}
