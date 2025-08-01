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
package org.opennms.reporting.availability;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class Day.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "day")
@XmlAccessorType(XmlAccessType.FIELD)
public class Day implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "date")
    private Integer date;

    @XmlAttribute(name = "pctValue")
    private Double pctValue;

    @XmlAttribute(name = "visible")
    private Boolean visible;

    public Day() {
    }

    /**
     */
    public void deleteDate() {
        this.date= null;
    }

    /**
     */
    public void deletePctValue() {
        this.pctValue= null;
    }

    /**
     */
    public void deleteVisible() {
        this.visible= null;
    }

    /**
     * Returns the value of field 'date'.
     * 
     * @return the value of field 'Date'.
     */
    public Integer getDate() {
        return this.date;
    }

    /**
     * Returns the value of field 'pctValue'.
     * 
     * @return the value of field 'PctValue'.
     */
    public Double getPctValue() {
        return this.pctValue;
    }

    /**
     * Returns the value of field 'visible'.
     * 
     * @return the value of field 'Visible'.
     */
    public Boolean getVisible() {
        return this.visible;
    }

    /**
     * Method hasDate.
     * 
     * @return true if at least one Date has been added
     */
    public boolean hasDate() {
        return this.date != null;
    }

    /**
     * Method hasPctValue.
     * 
     * @return true if at least one PctValue has been added
     */
    public boolean hasPctValue() {
        return this.pctValue != null;
    }

    /**
     * Method hasVisible.
     * 
     * @return true if at least one Visible has been added
     */
    public boolean hasVisible() {
        return this.visible != null;
    }

    /**
     * Returns the value of field 'visible'.
     * 
     * @return the value of field 'Visible'.
     */
    public Boolean isVisible() {
        return this.visible;
    }

    /**
     * Sets the value of field 'date'.
     * 
     * @param date the value of field 'date'.
     */
    public void setDate(final Integer date) {
        this.date = date;
    }

    /**
     * Sets the value of field 'pctValue'.
     * 
     * @param pctValue the value of field 'pctValue'.
     */
    public void setPctValue(final Double pctValue) {
        this.pctValue = pctValue;
    }

    /**
     * Sets the value of field 'visible'.
     * 
     * @param visible the value of field 'visible'.
     */
    public void setVisible(final Boolean visible) {
        this.visible = visible;
    }

}
