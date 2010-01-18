//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
// 
// Created: December 8th, 2009 jonathan@opennms.org
//
// Copyright (C) 2009 The OpenNMS Group, Inc. All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//
package org.opennms.api.reporting;

import java.io.Serializable;

public class DeliveryOptions implements Serializable {

    private static final long serialVersionUID = 7983363859009905407L;

    protected String m_mailTo;
    protected Boolean m_persist;
    protected Boolean m_canPersist;
    protected Boolean m_sendMail;
    protected String m_mailFormat;
    protected ReportFormat m_format;

    public String getMailTo() {
        return m_mailTo;
    }

    public void setMailTo(String email) {
        m_mailTo = email;
    }

    public String getMailFormat() {
        return m_mailFormat;
    }

    public void setMailFormat(String format) {
        m_mailFormat = format;
    }

    public void setCanPersist(Boolean canPersist) {
        m_canPersist = canPersist;
    }

    public Boolean getCanPersist() {
        return m_canPersist;
    }

    public void setPersist(Boolean persist) {
        m_persist = persist;
    }

    public Boolean getPersist() {
        return m_persist;
    }

    public void setSendMail(Boolean sendEmail) {
        m_sendMail = sendEmail;
    }

    public Boolean getSendMail() {
        return m_sendMail;
    }

    public ReportFormat getFormat() {
        return m_format;
    }

    public void setFormat(ReportFormat format) {
        m_format = format;
    }

}
