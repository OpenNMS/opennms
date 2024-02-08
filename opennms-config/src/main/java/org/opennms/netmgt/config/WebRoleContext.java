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


/**
 * <p>AppContext class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class WebRoleContext {
    private static WebRoleManagerImpl s_manager = null;
    
    /**
     * <p>init</p>
     *
     * @throws java.lang.Exception if any.
     */
    public static void init() throws Exception {
        GroupFactory.init();
        UserFactory.init();
    }
    
    private static WebRoleManagerImpl getManager() {
        if (s_manager == null) {
            s_manager = new WebRoleManagerImpl(GroupFactory.getInstance(), UserFactory.getInstance());
        }
        
        return s_manager;
    }
    
    /**
     * <p>getWebRoleManager</p>
     *
     * @return a {@link org.opennms.netmgt.config.WebRoleManager} object.
     */
    public static WebRoleManager getWebRoleManager() {
        return getManager();
    }

    /**
     * <p>getWebUserManager</p>
     *
     * @return a {@link org.opennms.netmgt.config.WebUserManager} object.
     */
    public static WebUserManager getWebUserManager() {
        return getManager();
    }
    
    /**
     * <p>getWebGroupManager</p>
     *
     * @return a {@link org.opennms.netmgt.config.WebGroupManager} object.
     */
    public static WebGroupManager getWebGroupManager() {
        return getManager();
    }

}
