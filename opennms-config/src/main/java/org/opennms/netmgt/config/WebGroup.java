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
package org.opennms.netmgt.config;

import java.util.Collection;

/**
 * <p>WebGroup class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class WebGroup {

    private final String m_name;
    private Collection<WebUser> m_users;
    
    /**
     * <p>Constructor for WebGroup.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public WebGroup(String name) {
        m_name = name;
    }
    
    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return ""+getName();
    }
    
    /**
     * <p>getUsers</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<WebUser> getUsers() {
        return m_users;
    }
    
    /**
     * <p>setUsers</p>
     *
     * @param users a {@link java.util.Collection} object.
     */
    protected void setUsers(Collection<WebUser> users) {
        m_users = users;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WebGroup) {
            WebGroup u = (WebGroup)obj;
            return m_name.equals(u.m_name);
        }
        return false;
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    @Override
    public int hashCode() {
        return m_name.hashCode();
    }


}
