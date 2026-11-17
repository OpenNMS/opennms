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
package org.opennms.netmgt.model.usermgmt;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The built-in security roles: the Spring Security authorities ({@code ROLE_*}) that ship with OpenNMS.
 * The 37.0.0 changelog seeds the {@code security_roles} table with exactly these, flagged {@code builtin};
 * custom roles are added beside them and can be deleted, built-in ones cannot.
 */
public final class SecurityRoles {

    public static final String ROLE_PREFIX = "ROLE_";

    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_READONLY = "ROLE_READONLY";
    public static final String ROLE_DASHBOARD = "ROLE_DASHBOARD";
    public static final String ROLE_DELEGATE = "ROLE_DELEGATE";
    public static final String ROLE_RTC = "ROLE_RTC";
    public static final String ROLE_PROVISION = "ROLE_PROVISION";
    public static final String ROLE_REST = "ROLE_REST";
    public static final String ROLE_ASSET_EDITOR = "ROLE_ASSET_EDITOR";
    public static final String ROLE_FILESYSTEM_EDITOR = "ROLE_FILESYSTEM_EDITOR";
    public static final String ROLE_MOBILE = "ROLE_MOBILE";
    public static final String ROLE_JMX = "ROLE_JMX";
    public static final String ROLE_MINION = "ROLE_MINION";
    public static final String ROLE_REPORT_DESIGNER = "ROLE_REPORT_DESIGNER";
    public static final String ROLE_FLOW_MANAGER = "ROLE_FLOW_MANAGER";
    public static final String ROLE_DEVICE_CONFIG_BACKUP = "ROLE_DEVICE_CONFIG_BACKUP";

    /**
     * Built-in role name to description, in the order {@code Authentication.getAvailableRoles()} has always
     * listed them. Must match the rows seeded by the 37.0.0 changelog ({@code SecurityRoleDaoIT} checks).
     */
    public static final Map<String, String> BUILTIN_ROLES;

    static {
        final Map<String, String> roles = new LinkedHashMap<>();
        roles.put(ROLE_USER, "Default permissions for a new user to interact with the web UI. Allows users to escalate and acknowledge alarms and notifications.");
        roles.put(ROLE_ADMIN, "Permissions to create, read, update, and delete items via the web UI and the REST API (see ROLE_FILESYSTEM_EDITOR for exceptions).");
        roles.put(ROLE_READONLY, "Permissions only to read information in the web UI. Does not allow user to change alarm states or notifications.");
        roles.put(ROLE_DASHBOARD, "Permissions only to view the dashboard.");
        roles.put(ROLE_DELEGATE, "Permissions to perform actions (such as acknowledging an alarm) on behalf of another user.");
        roles.put(ROLE_RTC, "Permissions to exchange information with the Real-Time Console for availability calculations.");
        roles.put(ROLE_PROVISION, "Permissions to use the provisioning system and configure SNMP to access management information from devices.");
        roles.put(ROLE_REST, "Permissions to interact with the REST API.");
        roles.put(ROLE_ASSET_EDITOR, "Permissions only to update asset records from nodes.");
        roles.put(ROLE_FILESYSTEM_EDITOR, "Permissions only to view and update file configuration data via the REST API.");
        roles.put(ROLE_MOBILE, "Permissions to use the OpenNMS COMPASS mobile application to acknowledge alarms and notifications via the REST API.");
        roles.put(ROLE_JMX, "Permissions to retrieve JMX metrics.");
        roles.put(ROLE_MINION, "Minimum permissions required for a Minion to operate.");
        roles.put(ROLE_REPORT_DESIGNER, "Permissions to manage reports in the web UI and REST API.");
        roles.put(ROLE_FLOW_MANAGER, "Permissions to edit flow classifications.");
        roles.put(ROLE_DEVICE_CONFIG_BACKUP, "Permissions to view and trigger device configuration backups.");
        BUILTIN_ROLES = Collections.unmodifiableMap(roles);
    }

    private SecurityRoles() {
    }

    public static boolean isBuiltin(final String roleName) {
        return BUILTIN_ROLES.containsKey(roleName);
    }

    /**
     * Turns a short role name as written in {@code security-roles.properties} ({@code operator}) into the
     * authority name ({@code ROLE_OPERATOR}). The input is always prefixed, as {@code Authentication} has always
     * done, so {@code role_x} becomes {@code ROLE_ROLE_X}; the migrated users reference that name.
     *
     * @throws IllegalArgumentException if the name is null or blank
     */
    public static String toRoleName(final String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A security role name must not be blank");
        }
        return ROLE_PREFIX + name.trim().toUpperCase(Locale.ROOT);
    }
}
