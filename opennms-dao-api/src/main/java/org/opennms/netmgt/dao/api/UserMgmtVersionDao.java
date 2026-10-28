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
package org.opennms.netmgt.dao.api;

/**
 * Reads the usermgmt_version counters. Triggers on the user tables increment them, so a changed value means that
 * some writer (in this JVM or not) changed the user data.
 */
public interface UserMgmtVersionDao {

    /** The rows of usermgmt_version; the constant names are the stored names. */
    enum Domain {
        /** users, their contacts, duty schedules and security roles, and the security_roles table */
        users,
        /** groups, their members and duty schedules, and the on-call roles with their schedules */
        groups
    }

    /** The tables with a trigger-maintained version column; the constant names are the table names. */
    enum VersionedTable {
        users,
        groups,
        oncall_roles,
        security_roles
    }

    long getVersion(Domain domain);

    /**
     * Locks the users row of usermgmt_version, then the groups row: the order every writer follows. Must be
     * called in a transaction. Takes the locks without writing out pending changes, so no trigger can bump a
     * version before the locks are held.
     */
    void lockAll();

    /**
     * Writes out pending changes, then reads the version column of a row.
     *
     * @throws IllegalArgumentException if the row doesn't exist
     */
    int getRowVersion(VersionedTable table, int id);
}
