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
package org.opennms.netmgt.mock;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author david
 */
public class MockNotification {

    private String m_subject;
    private String m_email;
    private String m_pemail;
	private String m_textMsg;
    private long m_expectedTime;
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 57)
            .append(m_subject)
            .append(m_email)
            .append(m_pemail)
            .append(m_textMsg)
            .append(m_expectedTime)
            .toHashCode();
    }

    @Override
    public boolean equals(final Object o) {
        
        if(o instanceof MockNotification) {
            final MockNotification m = (MockNotification)o;
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
    public void setEmail(final String email) {
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
    public void setPmail(final String pmail) {
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
    public void setSubject(final String subject) {
        m_subject = subject;
    }
    
    public long getExpectedTime() {
        return m_expectedTime;
    }

    public void setExpectedTime(final long expectedTime) {
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
	public void setTextMsg(final String textMsg) {
		m_textMsg = textMsg;
	}

	@Override
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
