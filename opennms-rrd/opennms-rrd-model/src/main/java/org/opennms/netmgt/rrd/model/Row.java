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
package org.opennms.netmgt.rrd.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * The Class Row.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
@XmlRootElement(name="row")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Row {

    /** The values. */
    private List<Double> values = new ArrayList<>();

    /**
     * Gets the values.
     *
     * @return the values
     */
    @XmlElement(name="v")
    public List<Double> getValues() {
        return values;
    }

    /**
     * Gets the value.
     *
     * @param index the index
     * @return the value
     */
    public Double getValue(int index) {
        return values.get(index);
    }

    /**
     * Sets the values.
     *
     * @param values the new values
     */
    public void setValues(List<Double> values) {
        this.values = values;
    }

    /**
     * Checks if is all the values are NaN.
     *
     * @return true, if all the values are NaN.
     */
    @XmlTransient
    public boolean isNan() {
        for (Double v : values) {
            if (!v.isNaN()) {
                return false;
            }
        }
        return true;
    }
}
