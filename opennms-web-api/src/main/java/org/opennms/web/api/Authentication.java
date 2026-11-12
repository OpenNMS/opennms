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
package org.opennms.web.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.opennms.netmgt.model.usermgmt.SecurityRoles;

/**
 * An uninstantiatable class that provides a servlet container-independent
 * interface to the authentication system and a list of useful constants.
 *
 * The valid roles come from the role source, which DatabaseSecurityRoles sets to the security_roles table. Without
 * a role source, only the built-in roles are valid.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="mailto:agalue@opennms.org">Alejandro Galue</A>
 */
public final class Authentication extends Object {

    public static final String ROLE_USER = SecurityRoles.ROLE_USER;
    public static final String ROLE_ADMIN = SecurityRoles.ROLE_ADMIN;
    public static final String ROLE_READONLY = SecurityRoles.ROLE_READONLY;
    public static final String ROLE_DASHBOARD = SecurityRoles.ROLE_DASHBOARD;
    public static final String ROLE_DELEGATE = SecurityRoles.ROLE_DELEGATE;
    public static final String ROLE_RTC = SecurityRoles.ROLE_RTC;
    public static final String ROLE_PROVISION = SecurityRoles.ROLE_PROVISION;
    public static final String ROLE_REST = SecurityRoles.ROLE_REST;
    public static final String ROLE_ASSET_EDITOR = SecurityRoles.ROLE_ASSET_EDITOR;
    public static final String ROLE_FILESYSTEM_EDITOR = SecurityRoles.ROLE_FILESYSTEM_EDITOR;
    public static final String ROLE_MOBILE = SecurityRoles.ROLE_MOBILE;
    public static final String ROLE_JMX = SecurityRoles.ROLE_JMX;
    public static final String ROLE_MINION = SecurityRoles.ROLE_MINION;
    public static final String ROLE_REPORT_DESIGNER = SecurityRoles.ROLE_REPORT_DESIGNER;
    public static final String ROLE_FLOW_MANAGER = SecurityRoles.ROLE_FLOW_MANAGER;
    public static final String ROLE_DEVICE_CONFIG_BACKUP = SecurityRoles.ROLE_DEVICE_CONFIG_BACKUP;

    private static final List<String> BUILTIN_ROLES = Collections.unmodifiableList(new ArrayList<>(SecurityRoles.BUILTIN_ROLES.keySet()));

    private static final long ROLE_SOURCE_CACHE_MS = 1000;

    private static volatile Supplier<Collection<String>> s_roleSource;
    private static volatile List<String> s_sourceRoles;
    private static volatile long s_sourceRolesLoaded;

    /** Private, empty constructor so this class cannot be instantiated. */
    private Authentication() {
    }

    /**
     * Sets where the valid roles come from. With null (the default), only the built-in roles are valid.
     * DatabaseSecurityRoles sets the security_roles table here.
     *
     * @param source the names of all valid roles
     */
    public static void setRoleSource(final Supplier<Collection<String>> source) {
        s_roleSource = source;
        s_sourceRoles = null;
    }

    /** @return true if {@link #setRoleSource(Supplier)} has set a role source */
    public static boolean hasRoleSource() {
        return s_roleSource != null;
    }

    public static List<String> getAvailableRoles() {
        final List<String> sourceRoles = sourceRoles();
        return sourceRoles != null ? sourceRoles : BUILTIN_ROLES;
    }

    public static boolean isValidRole(String role) {
        return getAvailableRoles().contains(role);
    }

    /** @return the roles of the role source, read at most once a second, or null without a role source */
    private static List<String> sourceRoles() {
        final Supplier<Collection<String>> source = s_roleSource;
        if (source == null) {
            return null;
        }
        final long now = System.currentTimeMillis();
        List<String> roles = s_sourceRoles;
        if (roles == null || now > s_sourceRolesLoaded + ROLE_SOURCE_CACHE_MS) {
            roles = Collections.unmodifiableList(new ArrayList<>(source.get()));
            s_sourceRoles = roles;
            s_sourceRolesLoaded = now;
        }
        return roles;
    }
}
