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
package org.opennms.web.svclayer.model;

import java.util.Date;

import org.opennms.api.reporting.parameter.ReportParameters;
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
    private ReportParameters m_reportParameters;
    private String cronExpression;

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

    public ReportParameters getReportParameters() {
        return m_reportParameters;
    }

    public void setReportParameters(ReportParameters reportParameters) {
        this.m_reportParameters = reportParameters;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getCronExpression() {
        return cronExpression;
    }
}
