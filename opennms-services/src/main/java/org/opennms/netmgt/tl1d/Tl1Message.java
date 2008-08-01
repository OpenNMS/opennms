/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created August 1, 2008
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.tl1d;

import java.util.Date;

public class Tl1Message {

    protected String m_message;
    protected String m_rawMessage;
    protected String m_severity;
    protected String m_equipment;
    protected String m_additionalParms;
    protected Date m_timestamp;
    protected String m_host;

    public String getHost() {
        return m_host;
    }

    public void setHost(String host) {
        m_host = host;
    }

    public String getSeverity() {
        return m_severity;
    }

    public void setSeverity(String severity) {
        m_severity = severity;
    }

    public Tl1Message() {
        super();
    }

    public String getMessage() {
        return m_message;
    }

    public void setMessage(String message) {
        m_message = message;
    }

    public String getRawMessage() {
        return m_rawMessage;
    }

    public void setRawMessage(String rawMessage) {
        m_rawMessage = rawMessage;
    }

    public String getEquipment() {
        return m_equipment;
    }

    public void setEquipment(String equipment) {
        m_equipment = equipment;
    }

    public String getAdditionalParms() {
        return m_additionalParms;
    }

    public void setAdditonalParms(String parms) {
        m_additionalParms = parms;
    }

    public Date getTimeStamp() {
        return m_timestamp;
    }

    public void setTimeStamp(Date date) {
        m_timestamp = date;
    }

    public String toString() {
        return m_message;
    }



}