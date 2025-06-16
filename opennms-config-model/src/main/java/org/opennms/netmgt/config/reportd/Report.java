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
package org.opennms.netmgt.config.reportd;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Defines an report schedule with a cron expression
 *  
 *  http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/crontrigger
 *  Field Name Allowed Values Allowed Special Characters
 *  Seconds 0-59 , - /
 *  Minutes 0-59 , - /
 *  Hours 0-23 , - /
 *  Day-of-month 1-31 , - ? / L W C
 *  Month 1-12 or JAN-DEC , - /
 *  Day-of-Week 1-7 or SUN-SAT , - ? / L C #
 *  Year (Opt) empty, 1970-2099 , - /
 */
@XmlRootElement(name = "report")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("reportd-configuration.xsd")
public class Report implements Serializable {
    private static final long serialVersionUID = 2L;

    private static final List<String> REPORT_FORMAT_TYPES = Arrays.asList("pdf", "csv", "html");
    private static final List<String> REPORT_ENGINE_TYPES = Arrays.asList("jdbc", "opennms");

    private static final String DEFAULT_REPORT_FORMAT = "pdf";
    private static final String DEFAULT_REPORT_ENGINE = "opennms";

    @XmlAttribute(name = "report-template", required = true)
    private String m_reportTemplate;

    @XmlAttribute(name = "report-name", required = true)
    private String m_reportName;

    @XmlAttribute(name = "report-format")
    private String m_reportFormat;

    @XmlAttribute(name = "report-engine")
    private String m_reportEngine;

    @XmlElement(name = "cron-schedule", required = true)
    private String m_cronSchedule;

    @XmlElement(name = "recipient")
    private List<String> m_recipients = new ArrayList<>();

    @XmlElement(name = "mailer")
    private String m_mailer;

    @XmlElement(name = "parameter")
    private List<Parameter> m_parameters = new ArrayList<>();

    public String getReportTemplate() {
        return m_reportTemplate;
    }

    public void setReportTemplate(final String reportTemplate) {
        m_reportTemplate = ConfigUtils.assertNotEmpty(reportTemplate, "report-template");
    }

    public String getReportName() {
        return m_reportName;
    }

    public void setReportName(final String reportName) {
        m_reportName = ConfigUtils.assertNotEmpty(reportName, "report-name");
    }

    public String getReportFormat() {
        return m_reportFormat != null ? m_reportFormat : DEFAULT_REPORT_FORMAT;
    }

    public void setReportFormat(final String reportFormat) {
        m_reportFormat = ConfigUtils.assertOnlyContains(reportFormat, REPORT_FORMAT_TYPES, "report-format");
    }

    public String getReportEngine() {
        return m_reportEngine != null ? m_reportEngine : DEFAULT_REPORT_ENGINE;
    }

    public void setReportEngine(final String reportEngine) {
        m_reportEngine = ConfigUtils.assertOnlyContains(reportEngine, REPORT_ENGINE_TYPES, "report-engine");
    }

    public String getCronSchedule() {
        return m_cronSchedule;
    }

    public void setCronSchedule(final String cronSchedule) {
        m_cronSchedule = ConfigUtils.assertNotEmpty(cronSchedule, "cron-schedule");
    }

    public List<String> getRecipients() {
        return m_recipients;
    }

    public void setRecipients(final List<String> recipients) {
        if (recipients == m_recipients) return;
        m_recipients.clear();
        if (recipients != null) m_recipients.addAll(recipients);
    }

    public void addRecipient(final String recipient) {
        m_recipients.add(recipient);
    }

    public boolean removeRecipient(final String recipient) {
        return m_recipients.remove(recipient);
    }

    public Optional<String> getMailer() {
        return Optional.ofNullable(m_mailer);
    }

    public void setMailer(final String mailer) {
        m_mailer = ConfigUtils.assertNotEmpty(mailer, "mailer");
    }

    public List<Parameter> getParameters() {
        return m_parameters;
    }

    public void setParameters(final List<Parameter> parameters) {
        if (parameters == m_parameters) return;
        m_parameters.clear();
        if (parameters != null) m_parameters.addAll(parameters);
    }

    public void addParameter(final Parameter parameter) {
        m_parameters.add(parameter);
    }

    public boolean removeParameter(final Parameter parameter) {
        return m_parameters.remove(parameter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            m_reportTemplate, 
                            m_reportName, 
                            m_reportFormat, 
                            m_reportEngine, 
                            m_cronSchedule, 
                            m_recipients, 
                            m_mailer, 
                            m_parameters);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Report) {
            final Report that = (Report)obj;
            return Objects.equals(this.m_reportTemplate, that.m_reportTemplate)
                    && Objects.equals(this.m_reportName, that.m_reportName)
                    && Objects.equals(this.m_reportFormat, that.m_reportFormat)
                    && Objects.equals(this.m_reportEngine, that.m_reportEngine)
                    && Objects.equals(this.m_cronSchedule, that.m_cronSchedule)
                    && Objects.equals(this.m_recipients, that.m_recipients)
                    && Objects.equals(this.m_mailer, that.m_mailer)
                    && Objects.equals(this.m_parameters, that.m_parameters);
        }
        return false;
    }

}
