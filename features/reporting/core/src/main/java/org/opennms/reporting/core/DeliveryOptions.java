/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.reporting.core;

import java.io.Serializable;

import org.opennms.api.reporting.ReportFormat;

public class DeliveryOptions implements Serializable {

    private static final long serialVersionUID = 7983363859009905407L;

    protected String m_mailTo;
    protected Boolean m_persist;
    protected Boolean m_sendMail;
    protected ReportFormat m_format;
    protected String m_instanceId;
    protected Boolean m_webhook;
    protected String m_webhookUrl;

    public String getMailTo() {
        return m_mailTo;
    }

    public void setMailTo(String email) {
        m_mailTo = email;
    }

    public void setPersist(boolean persist) {
        m_persist = persist;
    }

    public boolean isPersist() {
        return m_persist == null ? false : m_persist;
    }

    public void setSendMail(boolean sendEmail) {
        m_sendMail = sendEmail;
    }

    public boolean isSendMail() {
        return m_sendMail == null ? false : m_sendMail;
    }

    public ReportFormat getFormat() {
        return m_format;
    }

    public void setFormat(ReportFormat format) {
        m_format = format;
    }

    public String getInstanceId() {
        return m_instanceId;
    }

    public void setInstanceId(String instanceId) {
        m_instanceId = instanceId;
    }

    public String getWebhookUrl() {
        return m_webhookUrl;
    }

    public boolean isWebhook() {
        return m_webhook == null ? false : m_webhook;
    }

    public Boolean getWebhook() {
        return m_webhook;
    }

    public void setWebhook(boolean webhook) {
        m_webhook = webhook;
    }

    public void setWebhookUrl(String url) {
        m_webhookUrl = url;
    }

}
