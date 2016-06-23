/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.model;

import java.util.Date;
import java.util.Map;

import org.opennms.reporting.core.DeliveryOptions;

/**
 * <p>TriggerDescription class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class TriggerDescription {

    private String m_triggerName;
    private String m_description;
    private String m_reportId;
    private Date m_nextFireTime;
    private DeliveryOptions m_deliveryOptions;
    private Map<String,Object> m_reportParameters;

    /**
     * <p>getTriggerName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTriggerName() {
        return m_triggerName;
    }

    /**
     * <p>setTriggerName</p>
     *
     * @param triggerName a {@link java.lang.String} object.
     */
    public void setTriggerName(String triggerName) {
        m_triggerName = triggerName;
    }

    /**
     * <p>getReportId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReportId() {
        return m_reportId;
    }

    /**
     * <p>setReportId</p>
     *
     * @param reportId a {@link java.lang.String} object.
     */
    public void setReportId(String reportId) {
        m_reportId = reportId;
    }

    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return m_description;
    }

    /**
     * <p>setDescription</p>
     *
     * @param description a {@link java.lang.String} object.
     */
    public void setDescription(String description) {
        m_description = description;
    }

    /**
     * <p>getNextFireTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getNextFireTime() {
        return m_nextFireTime;
    }

    /**
     * <p>setNextFireTime</p>
     *
     * @param nextFireTime a {@link java.util.Date} object.
     */
    public void setNextFireTime(Date nextFireTime) {
        m_nextFireTime = nextFireTime;
    }

    /**
     * <p>getDeliveryOptions</p>
     *
     * @return a {@link org.opennms.reporting.core.DeliveryOptions} object.
     */
    public DeliveryOptions getDeliveryOptions() {
        return m_deliveryOptions;
    }

    /**
     * <p>setDeliveryOptions</p>
     *
     * @param deliveryOptions a {@link org.opennms.reporting.core.DeliveryOptions} object.
     */
    public void setDeliveryOptions(DeliveryOptions deliveryOptions) {
        this.m_deliveryOptions = deliveryOptions;
    }

    /**
     * <p>getReportParameters</p>
     *
     * @return a {@link java.util.Map<String, Object>} object.
     */
    public Map<String,Object> getReportParameters() {
        return m_reportParameters;
    }

    /**
     * <p>setReportParameters</p>
     *
     * @param reportParameters a {@link java.util.Map<String, Object>} object.
     */
    public void setReportParameters(Map<String,Object> reportParameters) {
        this.m_reportParameters = reportParameters;
    }

}
