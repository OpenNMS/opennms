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
 * <p>WebRoleManager interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface WebRoleManager {
    
    /**
     * <p>getRoles</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<WebRole> getRoles();

    /**
     * <p>deleteRole</p>
     *
     * @param roleName a {@link java.lang.String} object.
     */
    public void deleteRole(String roleName);

    /**
     * <p>getRole</p>
     *
     * @param roleName a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.admin.roles.WebRole} object.
     */
    public WebRole getRole(String roleName);

    /**
     * <p>saveRole</p>
     *
     * @param role a {@link org.opennms.web.admin.roles.WebRole} object.
     */
    public void saveRole(WebRole role);

    /**
     * <p>createRole</p>
     *
     * @return a {@link org.opennms.web.admin.roles.WebRole} object.
     */
    public WebRole createRole();

}
