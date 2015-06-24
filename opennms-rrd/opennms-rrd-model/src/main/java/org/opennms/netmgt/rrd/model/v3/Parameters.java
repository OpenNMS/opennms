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
 * The Class RRA Parameters.
 * 
 * <ul>
 * <li><b>param.hwpredict:</b> hw_alpha, hw_beta, dependent_rra_idx</li>
 * <li><b>param.seasonal:</b> seasonal_gamma, seasonal_smooth_idx, smoothing_window?, dependent_rra_idx</li>
 * <li><b>param.failures:</b> delta_pos, delta_neg, window_len, failure_threshold</li>
 * <li><b>param.devpredict:</b> dependent_rra_idx</li>
 * <li><b>param.avg_min_max:</b> xff</li>
 * </ul>
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
@XmlRootElement(name="params")
@XmlAccessorType(XmlAccessType.FIELD)
public class Parameters {

    /** The delta negative (CF_FAILURES). */
    @XmlElement(name="delta_neg")
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    private Double deltaNeg;

    /** The delta position (CF_FAILURES). */
    @XmlElement(name="delta_pos")
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    private Double deltaPos;

    /** The dependent RRA index (CF_SEASONAL, CF_DEVSEASONAL, CF_HWPREDICT, CF_MHWPREDICT, CF_DEVPREDICT). */
    @XmlElement(name="dependent_rra_idx")
    @XmlJavaTypeAdapter(LongAdapter.class)
    private Long dependentRraIdx;

    /** The failure threshold (CF_FAILURES). */
    @XmlElement(name="failure_threshold")
    @XmlJavaTypeAdapter(LongAdapter.class)
    private Long failureThreshold;

    /** The HW alpha (CF_HWPREDICT, CF_MHWPREDICT). */
    @XmlElement(name="hw_alpha")
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    private Double hwAlpha;

    /** The HW beta (CF_HWPREDICT, CF_MHWPREDICT). */
    @XmlElement(name="hw_beta")
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    private Double hwBeta;

    /** The seasonal gamma (CF_SEASONAL, CF_DEVSEASONAL). */
    @XmlElement(name="seasonal_gamma")
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    private Double seasonalGamma;

    /** The seasonal smooth index (CF_SEASONAL, CF_DEVSEASONAL). */
    @XmlElement(name="seasonal_smooth_idx")
    @XmlJavaTypeAdapter(LongAdapter.class)
    private Long seasonalSmoothIdx;

    /** The smoothing window (CF_SEASONAL, CF_DEVSEASONAL). */
    @XmlElement(name="smoothing_window")
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    private Double smoothingWindow;

    /** The window length (CF_FAILURES). */
    @XmlElement(name="window_len")
    @XmlJavaTypeAdapter(LongAdapter.class)
    private Long windowLen;

    /** The XFF (CF_AVERAGE, CF_MAXIMUM, CF_MINIMUM, CF_LAST). */
    @XmlElement(name="xff")
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    private Double xff = 0.5;

    /**
     * Gets the delta negative.
     *
     * @return the delta negative
     */
    public Double getDeltaNeg() {
        return deltaNeg;
    }

    /**
     * Sets the delta negative.
     *
     * @param deltaNeg the new delta negative
     */
    public void setDeltaNeg(Double deltaNeg) {
        this.deltaNeg = deltaNeg;
    }

    /**
     * Gets the delta position.
     *
     * @return the delta position
     */
    public Double getDeltaPos() {
        return deltaPos;
    }

    /**
     * Sets the delta position.
     *
     * @param deltaPos the new delta position
     */
    public void setDeltaPos(Double deltaPos) {
        this.deltaPos = deltaPos;
    }

    /**
     * Gets the dependent RRA index.
     *
     * @return the dependent RRA index
     */
    public Long getDependentRraIdx() {
        return dependentRraIdx;
    }

    /**
     * Sets the dependent RRA index.
     *
     * @param dependentRraIdx the new dependent RRA index
     */
    public void setDependentRraIdx(Long dependentRraIdx) {
        this.dependentRraIdx = dependentRraIdx;
    }

    /**
     * Gets the failure threshold.
     *
     * @return the failure threshold
     */
    public Long getFailureThreshold() {
        return failureThreshold;
    }

    /**
     * Sets the failure threshold.
     *
     * @param failureThreshold the new failure threshold
     */
    public void setFailureThreshold(Long failureThreshold) {
        this.failureThreshold = failureThreshold;
    }

    /**
     * Gets the hw alpha.
     *
     * @return the hw alpha
     */
    public Double getHwAlpha() {
        return hwAlpha;
    }

    /**
     * Sets the hw alpha.
     *
     * @param hwAlpha the new hw alpha
     */
    public void setHwAlpha(Double hwAlpha) {
        this.hwAlpha = hwAlpha;
    }

    /**
     * Gets the hw beta.
     *
     * @return the hw beta
     */
    public Double getHwBeta() {
        return hwBeta;
    }

    /**
     * Sets the hw beta.
     *
     * @param hwBeta the new hw beta
     */
    public void setHwBeta(Double hwBeta) {
        this.hwBeta = hwBeta;
    }

    /**
     * Gets the seasonal gamma.
     *
     * @return the seasonal gamma
     */
    public Double getSeasonalGamma() {
        return seasonalGamma;
    }

    /**
     * Sets the seasonal gamma.
     *
     * @param seasonalGamma the new seasonal gamma
     */
    public void setSeasonalGamma(Double seasonalGamma) {
        this.seasonalGamma = seasonalGamma;
    }

    /**
     * Gets the seasonal smooth index.
     *
     * @return the seasonal smooth index
     */
    public Long getSeasonalSmoothIdx() {
        return seasonalSmoothIdx;
    }

    /**
     * Sets the seasonal smooth index.
     *
     * @param seasonalSmoothIdx the new seasonal smooth index
     */
    public void setSeasonalSmoothIdx(Long seasonalSmoothIdx) {
        this.seasonalSmoothIdx = seasonalSmoothIdx;
    }

    /**
     * Gets the smoothing window.
     *
     * @return the smoothing window
     */
    public Double getSmoothingWindow() {
        return smoothingWindow;
    }

    /**
     * Sets the smoothing window.
     *
     * @param smoothingWindow the new smoothing window
     */
    public void setSmoothingWindow(Double smoothingWindow) {
        this.smoothingWindow = smoothingWindow;
    }

    /**
     * Gets the window length.
     *
     * @return the window length
     */
    public Long getWindowLen() {
        return windowLen;
    }

    /**
     * Sets the window length.
     *
     * @param windowLen the new window length
     */
    public void setWindowLen(Long windowLen) {
        this.windowLen = windowLen;
    }

    /**
     * Gets the XFF.
     * 
     * <p>XFF The xfiles factor defines what part of a consolidation interval may be made up from *UNKNOWN* data while the consolidated
     * value is still regarded as known. It is given as the ratio of allowed *UNKNOWN* PDPs to the number of PDPs in the interval.
     * Thus, it ranges from 0 to 1 (exclusive).</p>
     *
     * @return the XFF
     */
    public Double getXff() {
        return xff;
    }

    /**
     * Sets the XFF.
     *
     * @param xff the new XFF
     */
    public void setXff(Double xff) {
        this.xff = xff;
    }

}
