/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.rrd.model.v3;

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
 * <ul>
 * <li><b>ds.cdp.hwpredict:</b> intercept, last_intercept, slope, last_slope, nan_count, last_nan_count</li>
 * <li><b>ds.cdp.seasonal:</b> seasonal, last_seasonal, init_flag</li>
 * <li><b>ds.cdp.failures:</b> history</li>
 * <li><b>ds.cdp.avg_min_max:</b> value, unknown_datapoints</li>
 * <li><b>ds.cdp:</b> primary_value, secondary_value, (ds.cdp.hwpredict | ds.cdp.seasonal | ds.cdp.failures | ds.cdp.avg_min_max)?</li>
 * </ul>
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
@XmlRootElement(name="ds")
@XmlAccessorType(XmlAccessType.FIELD)
public class RRADS {

    /** The history (CF_FAILURES). */
    @XmlElement(name="history")
    private String history;

    /** The init seasonal (CF_SEASONAL, CF_DEVSEASONAL). */
    @XmlElement(name="init_flag")
    @XmlJavaTypeAdapter(LongAdapter.class)
    private Long initFlag = 1L;

    /** The intercept (CF_HWPREDICT, CF_MHWPREDICT). */
    @XmlElement(name="intercept")
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    private Double intercept = Double.NaN;

    /** The last intercept (CF_HWPREDICT, CF_MHWPREDICT). */
    @XmlElement(name="last_intercept")
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    private Double lastIntercept = Double.NaN;

    /** The last NaN count (CF_HWPREDICT, CF_MHWPREDICT). */
    @XmlElement(name="last_nan_count")
    @XmlJavaTypeAdapter(LongAdapter.class)
    private Long lastNanCount = 1L;

    /** The last seasonal (CF_SEASONAL, CF_DEVSEASONAL). */
    @XmlElement(name="last_seasonal")
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    private Double lastSeasonal = Double.NaN;

    /** The last slope (CF_HWPREDICT, CF_MHWPREDICT). */
    @XmlElement(name="last_slope")
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    private Double lastSlope = Double.NaN;

    /** The NaN count (CF_HWPREDICT, CF_MHWPREDICT). */
    @XmlElement(name="nan_count")
    @XmlJavaTypeAdapter(LongAdapter.class)
    private Long nanCount = 1L;

    /** The primary value (ALL). */
    @XmlElement(name="primary_value")
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    private Double primaryValue = 0.0;

    /** The seasonal (CF_SEASONAL, CF_DEVSEASONAL). */
    @XmlElement(name="seasonal")
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    private Double seasonal = Double.NaN;

    /** The secondary value (ALL). */
    @XmlElement(name="secondary_value")
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    private Double secondaryValue = 0.0;

    /** The slope (CF_HWPREDICT, CF_MHWPREDICT). */
    @XmlElement(name="slope")
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    private Double slope = Double.NaN;

    /** The unknown data points (CF_AVERAGE, CF_MAXIMUM, CF_MINIMUM, CF_LAST). */
    @XmlElement(name="unknown_datapoints")
    @XmlJavaTypeAdapter(LongAdapter.class)
    private Long unknownDataPoints = 0L;

    /** The value (CF_AVERAGE, CF_MAXIMUM, CF_MINIMUM, CF_LAST). */
    @XmlElement(name="value")
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    private Double value;

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
     * Gets the init seasonal.
     *
     * @return the init seasonal
     */
    public Long getInitFlag() {
        return initFlag;
    }

    /**
     * Sets the init seasonal.
     *
     * @param initFlag the new init seasonal
     */
    public void setInitFlag(Long initFlag) {
        this.initFlag = initFlag;
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
     * Gets the last NaN count.
     *
     * @return the last NaN count
     */
    public Long getLastNanCount() {
        return lastNanCount;
    }

    /**
     * Sets the last NaN count.
     *
     * @param lastNanCount the new last NaN count
     */
    public void setLastNanCount(Long lastNanCount) {
        this.lastNanCount = lastNanCount;
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
     * Gets the NaN count.
     *
     * @return the NaN count
     */
    public Long getNanCount() {
        return nanCount;
    }

    /**
     * Sets the NaN count.
     *
     * @param nanCount the new NaN count
     */
    public void setNanCount(Long nanCount) {
        this.nanCount = nanCount;
    }

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

}
