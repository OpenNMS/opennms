/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2004-2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.notifd.mock;
/**
 * @author david
 */
public class MockNotification {

    private String m_subject;
    private String m_email;
    private String m_pemail;
	private String m_textMsg;
    private long m_expectedTime;
    
    
    public boolean equals(Object o) {
        
        if(o instanceof MockNotification) {
            MockNotification m = (MockNotification)o;
            return (m_subject == null ? m.m_subject == null : m_subject.equals(m.m_subject))
                && (m_textMsg == null ? m.m_textMsg == null : m_textMsg.equals(m.m_textMsg))
                && (m_email == null ? m.m_email == null : m_email.equals(m.m_email))
                && (m_pemail == null ? m.m_pemail == null : m_pemail.equals(m.m_pemail));
        }
        return false;
        
    }
    
    /**
     * @return Returns the email.
     */
    public String getEmail() {
        return m_email;
    }
    /**
     * @param email The email to set.
     */
    public void setEmail(String email) {
        m_email = email;
    }
    /**
     * @return Returns the pmail.
     */
    public String getPmail() {
        return m_pemail;
    }
    /**
     * @param pmail The pmail to set.
     */
    public void setPmail(String pmail) {
        m_pemail = pmail;
    }
    /**
     * @return Returns the subject.
     */
    public String getSubject() {
        return m_subject;
    }
    /**
     * @param subject The subject to set.
     */
    public void setSubject(String subject) {
        m_subject = subject;
    }
    
    public long getExpectedTime() {
        return m_expectedTime;
    }

    public void setExpectedTime(long expectedTime) {
        m_expectedTime = expectedTime;
    }

	/**
	 * @return Returns the m_textMsg.
	 */
	public String getTextMsg() {
		return m_textMsg;
	}
	

	/**
	 * @param textMsg The m_textMsg to set.
	 */
	public void setTextMsg(String textMsg) {
		m_textMsg = textMsg;
	}

    public String toString() {
        return 
        "[" +
        " expectedTime = '" + m_expectedTime + "'" +
        " subject = '" + m_subject + "'" +
        " email = '" + m_email + "'" +
        " textMsg = '" + m_textMsg + "'" +
        "]";
    }
	
	
}
