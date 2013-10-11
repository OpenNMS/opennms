/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.rrdtool;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class RraDS (RRA Data Source).
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
@XmlRootElement(name="ds")
@XmlAccessorType(XmlAccessType.FIELD)
public class RraDS {

    /** The primary value. */
    @XmlElement(name="primary_value")
    private Double primaryValue = 0.0;

    /** The secondary value. */
    @XmlElement(name="secondary_value")
    private Double secondaryValue = 0.0;

    /** The value. */
    @XmlElement(name="value")
    private Double value;

    /** The unknown data points. */
    @XmlElement(name="unknown_datapoints")
    private Long unknownDataPoints = 0L;

    /** The history. */
    @XmlElement(name="history")
    private String history; // TODO Is String the proper type here ?

    /** The intercept. */
    @XmlElement(name="intercept")
    private Double intercept = Double.NaN;

    /** The last intercept. */
    @XmlElement(name="last_intercept")
    private Double lastIntercept = Double.NaN;

    /** The slope. */
    @XmlElement(name="slope")
    private Double slope = Double.NaN;

    /** The last slope. */
    @XmlElement(name="last_slope")
    private Double lastSlope = Double.NaN;

    /** The nan count. */
    @XmlElement(name="nan_count")
    private Integer nanCount = 1;

    /** The last nan count. */
    @XmlElement(name="last_nan_count")
    private Integer lastNanCount = 1;

    /** The seasonal. */
    @XmlElement(name="seasonal")
    private Double seasonal = Double.NaN;

    /** The last seasonal. */
    @XmlElement(name="last_seasonal")
    private Double lastSeasonal = Double.NaN;

    /** The init flag. */
    @XmlElement(name="init_flag")
    private Integer initFlag = 1;

    /**
     * Gets the primary value.
     *
     * @return the primary value
     */
    public Double getPrimaryValue() {
        return primaryValue;
    }

    /**
     * Sets the primary value.
     *
     * @param primaryValue the new primary value
     */
    public void setPrimaryValue(Double primaryValue) {
        this.primaryValue = primaryValue;
    }

    /**
     * Gets the secondary value.
     *
     * @return the secondary value
     */
    public Double getSecondaryValue() {
        return secondaryValue;
    }

    /**
     * Sets the secondary value.
     *
     * @param secondaryValue the new secondary value
     */
    public void setSecondaryValue(Double secondaryValue) {
        this.secondaryValue = secondaryValue;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
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

    /**
     * Gets the unknown data points.
     *
     * @return the unknown data points
     */
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
     * Gets the history.
     *
     * @return the history
     */
    public String getHistory() {
        return history;
    }

    /**
     * Sets the history.
     *
     * @param history the new history
     */
    public void setHistory(String history) {
        this.history = history;
    }

    /**
     * Gets the intercept.
     *
     * @return the intercept
     */
    public Double getIntercept() {
        return intercept;
    }

    /**
     * Sets the intercept.
     *
     * @param intercept the new intercept
     */
    public void setIntercept(Double intercept) {
        this.intercept = intercept;
    }

    /**
     * Gets the last intercept.
     *
     * @return the last intercept
     */
    public Double getLastIntercept() {
        return lastIntercept;
    }

    /**
     * Sets the last intercept.
     *
     * @param lastIntercept the new last intercept
     */
    public void setLastIntercept(Double lastIntercept) {
        this.lastIntercept = lastIntercept;
    }

    /**
     * Gets the slope.
     *
     * @return the slope
     */
    public Double getSlope() {
        return slope;
    }

    /**
     * Sets the slope.
     *
     * @param slope the new slope
     */
    public void setSlope(Double slope) {
        this.slope = slope;
    }

    /**
     * Gets the last slope.
     *
     * @return the last slope
     */
    public Double getLastSlope() {
        return lastSlope;
    }

    /**
     * Sets the last slope.
     *
     * @param lastSlope the new last slope
     */
    public void setLastSlope(Double lastSlope) {
        this.lastSlope = lastSlope;
    }

    /**
     * Gets the nan count.
     *
     * @return the nan count
     */
    public Integer getNanCount() {
        return nanCount;
    }

    /**
     * Sets the nan count.
     *
     * @param nanCount the new nan count
     */
    public void setNanCount(Integer nanCount) {
        this.nanCount = nanCount;
    }

    /**
     * Gets the last nan count.
     *
     * @return the last nan count
     */
    public Integer getLastNanCount() {
        return lastNanCount;
    }

    /**
     * Sets the last nan count.
     *
     * @param lastNanCount the new last nan count
     */
    public void setLastNanCount(Integer lastNanCount) {
        this.lastNanCount = lastNanCount;
    }

    /**
     * Gets the seasonal.
     *
     * @return the seasonal
     */
    public Double getSeasonal() {
        return seasonal;
    }

    /**
     * Sets the seasonal.
     *
     * @param seasonal the new seasonal
     */
    public void setSeasonal(Double seasonal) {
        this.seasonal = seasonal;
    }

    /**
     * Gets the last seasonal.
     *
     * @return the last seasonal
     */
    public Double getLastSeasonal() {
        return lastSeasonal;
    }

    /**
     * Sets the last seasonal.
     *
     * @param lastSeasonal the new last seasonal
     */
    public void setLastSeasonal(Double lastSeasonal) {
        this.lastSeasonal = lastSeasonal;
    }

    /**
     * Gets the inits the flag.
     *
     * @return the inits the flag
     */
    public Integer getInitFlag() {
        return initFlag;
    }

    /**
     * Sets the inits the flag.
     *
     * @param initFlag the new inits the flag
     */
    public void setInitFlag(Integer initFlag) {
        this.initFlag = initFlag;
    }

}
