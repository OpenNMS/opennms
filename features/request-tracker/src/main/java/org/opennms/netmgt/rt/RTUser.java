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
package org.opennms.netmgt.rt;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

public class RTUser implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -4830811460415894277L;
	private long m_id;
    private String m_username;
    private String m_realname;
    private String m_email;

    public RTUser(final long id, String username, String realname, String email) {
        m_id = id;
        m_username = username;
        m_realname = realname;
        m_email = email;
    }

    public long getId() {
        return m_id;
    }
    
    public String getUsername() {
        return m_username;
    }
    
    public String getRealname() {
        return m_realname;
    }
    
    public String getEmail() {
        return m_email;
    }
    
        @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", m_id)
            .append("username", m_username)
            .append("realname", m_realname)
            .append("email", m_email)
            .toString();
    }
}
