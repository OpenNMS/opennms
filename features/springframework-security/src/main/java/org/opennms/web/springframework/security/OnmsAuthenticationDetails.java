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
package org.opennms.web.springframework.security;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;

public class OnmsAuthenticationDetails extends WebAuthenticationDetails {
    private static final long serialVersionUID = -7850100700631490637L;

   private String[] m_userGroupList;

    /**
     * Records the remote address and will also set the session Id if a session
     * already exists (it won't create one).
     *
     * @param request that the authentication request was received from
     */
    public OnmsAuthenticationDetails(HttpServletRequest request) {
        super(request);
        String userGroups = request.getParameter("j_usergroups");
        if(userGroups != null){
            String[] split = userGroups.split(",");
            m_userGroupList = split[0].equals("") ? null : split;
        } else {
            m_userGroupList = null;
        }

    }

    public String[] getUserGroups(){
        return m_userGroupList;
    }

    public void setUserGroups(String... groups){
        m_userGroupList = groups;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append(": ");
        sb.append("UserGroups: ").append(this.getUserGroups());

        return sb.toString();
    }
}
