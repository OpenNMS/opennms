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

/**
 * <p>DeliveryOptions class.</p>
 */
public class DeliveryOptions implements Serializable {

    private static final long serialVersionUID = 7983363859009905407L;

    protected String m_mailTo;
    protected Boolean m_persist;
    protected Boolean m_sendMail;
    protected ReportFormat m_format;
    protected String m_instanceId;

    /**
     * <p>getMailTo</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMailTo() {
        return m_mailTo;
    }

    /**
     * <p>setMailTo</p>
     *
     * @param email a {@link java.lang.String} object.
     */
    public void setMailTo(String email) {
        m_mailTo = email;
    }

    /**
     * <p>setPersist</p>
     *
     * @param persist a {@link java.lang.Boolean} object.
     */
    public void setPersist(Boolean persist) {
        m_persist = persist;
    }

    /**
     * <p>getPersist</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getPersist() {
        return m_persist;
    }

    /**
     * <p>setSendMail</p>
     *
     * @param sendEmail a {@link java.lang.Boolean} object.
     */
    public void setSendMail(Boolean sendEmail) {
        m_sendMail = sendEmail;
    }

    /**
     * <p>getSendMail</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getSendMail() {
        return m_sendMail;
    }

    /**
     * <p>getFormat</p>
     *
     * @return a {@link org.opennms.api.reporting.ReportFormat} object.
     */
    public ReportFormat getFormat() {
        return m_format;
    }

    /**
     * <p>setFormat</p>
     *
     * @param format a {@link org.opennms.api.reporting.ReportFormat} object.
     */
    public void setFormat(ReportFormat format) {
        m_format = format;
    }

    /**
     * <p>getInstanceId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInstanceId() {
        return m_instanceId;
    }

    /**
     * <p>setInstanceId</p>
     *
     * @param instanceId a {@link java.lang.String} object.
     */
    public void setInstanceId(String instanceId) {
        m_instanceId = instanceId;
    }

}
