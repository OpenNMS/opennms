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
