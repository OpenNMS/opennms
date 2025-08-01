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
import javax.xml.bind.annotation.XmlValue;

/**
 * Class Created.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "created")
@XmlAccessorType(XmlAccessType.FIELD)
public class Created implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * internal content storage
     */
    @XmlValue
    private java.math.BigDecimal _content;

    @XmlAttribute(name = "year", required = true)
    private Integer year;

    @XmlAttribute(name = "month", required = true)
    private String month;

    @XmlAttribute(name = "day", required = true)
    private Integer day;

    @XmlAttribute(name = "hour", required = true)
    private Integer hour;

    @XmlAttribute(name = "min", required = true)
    private Integer min;

    @XmlAttribute(name = "sec", required = true)
    private Integer sec;

    @XmlAttribute(name = "period", required = true)
    private String period;

    public Created() {
    }

    /**
     */
    public void deleteDay() {
        this.day= null;
    }

    /**
     */
    public void deleteHour() {
        this.hour= null;
    }

    /**
     */
    public void deleteMin() {
        this.min= null;
    }

    /**
     */
    public void deleteSec() {
        this.sec= null;
    }

    /**
     */
    public void deleteYear() {
        this.year= null;
    }

    /**
     * Returns the value of field 'content'. The field 'content' has the following
     * description: internal content storage
     * 
     * @return the value of field 'Content'.
     */
    public java.math.BigDecimal getContent() {
        return this._content;
    }

    /**
     * Returns the value of field 'day'.
     * 
     * @return the value of field 'Day'.
     */
    public Integer getDay() {
        return this.day;
    }

    /**
     * Returns the value of field 'hour'.
     * 
     * @return the value of field 'Hour'.
     */
    public Integer getHour() {
        return this.hour;
    }

    /**
     * Returns the value of field 'min'.
     * 
     * @return the value of field 'Min'.
     */
    public Integer getMin() {
        return this.min;
    }

    /**
     * Returns the value of field 'month'.
     * 
     * @return the value of field 'Month'.
     */
    public String getMonth() {
        return this.month;
    }

    /**
     * Returns the value of field 'period'.
     * 
     * @return the value of field 'Period'.
     */
    public String getPeriod() {
        return this.period;
    }

    /**
     * Returns the value of field 'sec'.
     * 
     * @return the value of field 'Sec'.
     */
    public Integer getSec() {
        return this.sec;
    }

    /**
     * Returns the value of field 'year'.
     * 
     * @return the value of field 'Year'.
     */
    public Integer getYear() {
        return this.year;
    }

    /**
     * Method hasDay.
     * 
     * @return true if at least one Day has been added
     */
    public boolean hasDay() {
        return this.day != null;
    }

    /**
     * Method hasHour.
     * 
     * @return true if at least one Hour has been added
     */
    public boolean hasHour() {
        return this.hour != null;
    }

    /**
     * Method hasMin.
     * 
     * @return true if at least one Min has been added
     */
    public boolean hasMin() {
        return this.min != null;
    }

    /**
     * Method hasSec.
     * 
     * @return true if at least one Sec has been added
     */
    public boolean hasSec() {
        return this.sec != null;
    }

    /**
     * Method hasYear.
     * 
     * @return true if at least one Year has been added
     */
    public boolean hasYear() {
        return this.year != null;
    }

    /**
     * Sets the value of field 'content'. The field 'content' has the following
     * description: internal content storage
     * 
     * @param content the value of field 'content'.
     */
    public void setContent(final java.math.BigDecimal content) {
        this._content = content;
    }

    /**
     * Sets the value of field 'day'.
     * 
     * @param day the value of field 'day'.
     */
    public void setDay(final Integer day) {
        this.day = day;
    }

    /**
     * Sets the value of field 'hour'.
     * 
     * @param hour the value of field 'hour'.
     */
    public void setHour(final Integer hour) {
        this.hour = hour;
    }

    /**
     * Sets the value of field 'min'.
     * 
     * @param min the value of field 'min'.
     */
    public void setMin(final Integer min) {
        this.min = min;
    }

    /**
     * Sets the value of field 'month'.
     * 
     * @param month the value of field 'month'.
     */
    public void setMonth(final String month) {
        this.month = month;
    }

    /**
     * Sets the value of field 'period'.
     * 
     * @param period the value of field 'period'.
     */
    public void setPeriod(final String period) {
        this.period = period;
    }

    /**
     * Sets the value of field 'sec'.
     * 
     * @param sec the value of field 'sec'.
     */
    public void setSec(final Integer sec) {
        this.sec = sec;
    }

    /**
     * Sets the value of field 'year'.
     * 
     * @param year the value of field 'year'.
     */
    public void setYear(final Integer year) {
        this.year = year;
    }

}
