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
package org.opennms.netmgt.rrd.model.v1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.netmgt.rrd.model.DoubleAdapter;
import org.opennms.netmgt.rrd.model.LongAdapter;

/**
 * The Class RraDS (RRA CDP Data Source).
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
@XmlRootElement(name="ds")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class RRADS {

    /** The unknown data points. */
    private Long unknownDataPoints = 0L;

    /** The value. */
    private Double value;

    /**
     * Gets the unknown data points.
     *
     * @return the unknown data points
     */
    @XmlElement(name="unknown_datapoints")
    @XmlJavaTypeAdapter(LongAdapter.class)
    public Long getUnknownDataPoints() {
        return unknownDataPoints;
    }

    /**
     * Sets the unknown data points.
     *
     * @param unknownDataPoints the new unknown data points
     */
    public void setUnknownDataPoints(Long unknownDataPoints) {
        this.unknownDataPoints = unknownDataPoints;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    @XmlElement(name="value")
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    public Double getValue() {
        return value;
    }

    /**
     * Sets the value.
     *
     * @param value the new value
     */
    public void setValue(Double value) {
        this.value = value;
    }

}
