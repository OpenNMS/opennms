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
package org.opennms.netmgt.events.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.config.trapd.Snmpv3User;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;
import org.opennms.netmgt.dao.api.TrapdConfigDao;

/**
 * Deletes SNMPv3 users from the trapd configuration.
 *
 * '--show' renders the configuration with the index value of each SNMPv3 user alongside its
 * server-assigned id; either can be handed to '-d'/'--delete'. Indexes are positional and
 * shift whenever the configuration changes, so prefer the id (or a unique prefix of it) when
 * scripting; ids are stable for the lifetime of the user.
 */
@Command(scope = "opennms", name = "trapd-del-config",
        description = "Delete an SNMPv3 user from the trapd configuration. "
                + "Use --show to render the current users with their index and id values, then pass "
                + "an index or an id to -d/--delete.")
@Service
public class TrapdDelConfigCommand implements Action {

    private static final String MASKED = "******";

    @Reference
    private TrapdConfigDao trapdConfigDao;

    @Option(name = "--show", aliases = "-s",
            description = "Render the current trapd configuration with the index value and id of each "
                    + "SNMPv3 user. This is the default when no --delete is given.")
    private boolean show;

    @Option(name = "-d", aliases = "--delete", multiValued = true,
            description = "Index or id of the SNMPv3 user to delete, as rendered by --show. An id may be "
                    + "abbreviated to any unambiguous prefix. Repeatable, e.g. '-d 1 -d 3'.")
    private List<String> deletes = new ArrayList<>();

    @Override
    public Object execute() {
        final TrapdConfiguration config = trapdConfigDao.getConfig();
        if (config == null) {
            System.out.println("Error: No trapd configuration is available.");
            return null;
        }

        if (deletes == null || deletes.isEmpty()) {
            // --show is the default (and only) action when nothing is being deleted.
            printConfig(config);
            return null;
        }

        final boolean deleted = delete(config);

        if (deleted && show) {
            System.out.println();
            printConfig(trapdConfigDao.getConfig());
        }
        return null;
    }

    // ---------------------------------------------------------------- delete

    /**
     * Resolves every requested index or id against the current configuration, removes the
     * SNMPv3 users they identify and persists the result. Returns true if the configuration
     * was saved.
     */
    private boolean delete(final TrapdConfiguration config) {
        final List<Snmpv3User> users = config.getSnmpv3UserCollection();
        if (users == null || users.isEmpty()) {
            System.out.println("Error: There are no SNMPv3 users to delete.");
            return false;
        }

        // Resolve and validate everything up front so a bad target never leaves a partial delete behind.
        final Set<Integer> positions = new TreeSet<>();
        for (final String target : deletes) {
            final Integer position = resolve(target, users);
            if (position == null) {
                return false;
            }
            positions.add(position);
        }

        // Remove from the highest position down so the remaining positions stay valid, keeping
        // enough state to put everything back if the save fails.
        final List<Integer> descending = new ArrayList<>(positions);
        Collections.reverse(descending);

        final List<String> removals = new ArrayList<>();
        final List<Snmpv3User> removed = new ArrayList<>();
        for (final Integer position : descending) {
            final Snmpv3User user = users.get(position);
            removals.add(String.format("[%d] %s", position + 1, describe(user)));
            removed.add(config.removeSnmpv3UserAt(position));
        }
        Collections.reverse(removals);

        try {
            trapdConfigDao.replaceConfig(config);
        } catch (final Exception e) {
            // getConfig() hands back the DAO's cached instance, so put the users back: otherwise a
            // failed save would leave them missing in memory until the next reload. descending and
            // removed are in the same order, so re-inserting in reverse restores every position.
            for (int i = removed.size() - 1; i >= 0; i--) {
                config.addSnmpv3User(descending.get(i), removed.get(i));
            }
            System.out.println("Failed to delete SNMPv3 user(s) from the trapd configuration!");
            System.out.println(e.getMessage());
            return false;
        }

        System.out.println("Successfully deleted SNMPv3 user(s) from the trapd configuration:");
        for (final String removal : removals) {
            System.out.println("  " + removal);
        }
        return true;
    }

    /**
     * Resolves a '-d' argument to a position in the user list. A plain number is a 1-based index
     * as rendered by --show; anything else is matched against the ids, exactly first and then as a
     * unique prefix. Prints an error and returns null when the target cannot be resolved.
     */
    private static Integer resolve(final String target, final List<Snmpv3User> users) {
        final String value = target == null ? "" : target.trim();
        if (value.isEmpty()) {
            System.out.println("Error: Missing index or id.");
            return null;
        }

        if (value.chars().allMatch(Character::isDigit)) {
            // Anything that does not fit an int cannot be a valid index either, so report it the same way.
            final int index;
            try {
                index = Integer.parseInt(value);
            } catch (final NumberFormatException e) {
                System.out.printf("Error: There is no SNMPv3 user with index '%s'. Valid indexes are 1 to %d.%n",
                        value, users.size());
                return null;
            }
            if (index < 1 || index > users.size()) {
                System.out.printf("Error: There is no SNMPv3 user with index '%d'. Valid indexes are 1 to %d.%n",
                        index, users.size());
                return null;
            }
            return index - 1;
        }

        final List<Integer> prefixMatches = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            final String id = users.get(i).getId();
            if (id == null) {
                continue;
            }
            if (id.equals(value)) {
                return i;
            }
            if (id.startsWith(value)) {
                prefixMatches.add(i);
            }
        }

        if (prefixMatches.size() == 1) {
            return prefixMatches.get(0);
        }
        if (prefixMatches.isEmpty()) {
            System.out.printf("Error: There is no SNMPv3 user with index or id '%s'.%n", target);
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        for (final Integer match : prefixMatches) {
            sb.append(String.format("%n    [%d] %s", match + 1, users.get(match).getId()));
        }
        System.out.printf("Error: The id '%s' is ambiguous, it matches %d users:%s%n",
                target, prefixMatches.size(), sb);
        return null;
    }

    // ------------------------------------------------------------------ show

    private static void printConfig(final TrapdConfiguration config) {
        if (config == null) {
            System.out.println("Error: No trapd configuration is available.");
            return;
        }

        System.out.println("Trapd configuration (not indexed, cannot be deleted):");
        System.out.println("  snmp-trap-address=" + config.getSnmpTrapAddress()
                + ", snmp-trap-port=" + config.getSnmpTrapPort()
                + ", new-suspect-on-trap=" + config.isNewSuspectOnTrap()
                + ", include-raw-message=" + config.isIncludeRawMessage());
        System.out.println("  threads=" + config.getThreads()
                + ", queue-size=" + config.getQueueSize()
                + ", batch-size=" + config.getBatchSize()
                + ", batch-interval=" + config.getBatchInterval());
        System.out.println();

        final List<Snmpv3User> users = config.getSnmpv3UserCollection();
        if (users == null || users.isEmpty()) {
            System.out.println("There are no SNMPv3 users configured.");
            return;
        }

        System.out.println("SNMPv3 users:");
        int index = 0;
        for (final Snmpv3User user : users) {
            index++;
            System.out.printf("  [%d] id=%s%n", index, user.getId() == null ? "(none)" : user.getId());
            System.out.println("      " + describe(user));
        }

        System.out.println();
        System.out.println("Delete a user with '-d <index>' (e.g. '-d 1') or '-d <id>' (any unambiguous prefix of");
        System.out.println("the id will do). Indexes are positional and change whenever the configuration changes,");
        System.out.println("so re-run --show before deleting by index.");
    }

    /**
     * One-line summary of the user, with passphrases masked. SCV references are shown
     * as-is since they are pointers rather than secrets.
     */
    private static String describe(final Snmpv3User user) {
        final StringBuilder sb = new StringBuilder();
        append(sb, "security-name", user.getSecurityName());
        append(sb, "security-level", user.getSecurityLevel());
        append(sb, "engine-id", user.getEngineId());
        append(sb, "auth-protocol", user.getAuthProtocol());
        append(sb, "auth-passphrase", mask(user.getAuthPassphrase()));
        append(sb, "privacy-protocol", user.getPrivacyProtocol());
        append(sb, "privacy-passphrase", mask(user.getPrivacyPassphrase()));
        return sb.length() == 0 ? "(no attributes set)" : sb.toString();
    }

    private static void append(final StringBuilder sb, final String name, final Object value) {
        if (value == null) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(", ");
        }
        sb.append(name).append('=').append(value);
    }

    private static String mask(final String value) {
        if (value == null) {
            return null;
        }
        return value.startsWith("${scv:") ? value : MASKED;
    }
}