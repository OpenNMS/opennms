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
package org.opennms.netmgt.snmp.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.config.snmp.Configuration;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.Range;
import org.opennms.netmgt.config.snmp.SnmpConfig;

/**
 * Deletes SNMP configuration definitions from snmp-config.xml by index.
 *
 * Definitions are indexed by their position in the configuration. '--show' renders the
 * configuration with those indexes, which can then be handed to '-d'/'--delete' to remove
 * the definition they identify.
 *
 * Indexes are positional, so they change whenever the configuration changes. Always run
 * '--show' immediately before deleting.
 */
@Command(scope = "opennms", name = "snmp-del-config",
        description = "Delete an SNMP configuration definition from snmp-config.xml by index. "
                + "Use --show to render the current definitions with their index values, then pass an "
                + "index to -d/--delete.")
@Service
public class SnmpDelConfigCommand implements Action {

    private static final String DEFAULT_LOCATION = "Default";
    private static final String MASKED = "******";

    @Reference
    private SnmpAgentConfigFactory snmpAgentConfigFactory;

    @Option(name = "--show", aliases = "-s",
            description = "Render the current SNMP configuration with the index value of each definition. "
                    + "This is the default when no --delete is given.")
    private boolean show;

    @Option(name = "-d", aliases = "--delete", multiValued = true,
            description = "Index of the definition to delete, as rendered by --show. "
                    + "Repeatable, e.g. '-d 1 -d 3'.")
    private List<String> deletes = new ArrayList<>();

    @Override
    public Object execute() {
        final SnmpConfig config = snmpAgentConfigFactory.getSnmpConfig();
        if (config == null) {
            System.out.println("Error: No SNMP configuration is available.");
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
            printConfig(snmpAgentConfigFactory.getSnmpConfig());
        }
        return null;
    }

    // ---------------------------------------------------------------- delete

    /**
     * Resolves every requested index against the current configuration, removes the
     * definitions they identify and persists the result. Returns true if the
     * configuration was saved.
     */
    private boolean delete(final SnmpConfig config) {
        final List<Definition> definitions = new ArrayList<>(config.getDefinitions());
        if (definitions.isEmpty()) {
            System.out.println("Error: There are no SNMP configuration definitions to delete.");
            return false;
        }

        // Resolve and validate every index up front so a bad index never leaves a partial delete behind.
        final Set<Integer> indexes = new TreeSet<>();
        for (final String target : deletes) {
            final Integer index = parseIndex(target);
            if (index == null) {
                return false;
            }
            if (index < 1 || index > definitions.size()) {
                System.out.printf("Error: There is no definition with index '%d'. Valid indexes are 1 to %d.%n",
                        index, definitions.size());
                return false;
            }
            indexes.add(index);
        }

        final List<String> removals = new ArrayList<>();
        final List<Definition> remaining = new ArrayList<>(definitions.size());
        for (int index = 1; index <= definitions.size(); index++) {
            final Definition definition = definitions.get(index - 1);
            if (indexes.contains(index)) {
                removals.add(String.format("[%d] definition at location '%s' (%s)",
                        index, location(definition), describeEntries(definition)));
            } else {
                remaining.add(definition);
            }
        }
        config.setDefinitions(remaining);

        try {
            snmpAgentConfigFactory.setAndSaveConfig(config);
        } catch (final Exception e) {
            System.out.println("Failed to delete SNMP configuration!");
            System.out.println(e.getMessage());
            return false;
        }

        System.out.println("Successfully deleted SNMP configuration:");
        for (final String removal : removals) {
            System.out.println("  " + removal);
        }
        return true;
    }

    private static Integer parseIndex(final String target) {
        final String value = target == null ? "" : target.trim();
        try {
            return Integer.valueOf(value);
        } catch (final NumberFormatException e) {
            System.out.printf("Error: Invalid index '%s'. Expected the index of a definition, e.g. '2'.%n", target);
            return null;
        }
    }

    // ------------------------------------------------------------------ show

    private static void printConfig(final SnmpConfig config) {
        if (config == null) {
            System.out.println("Error: No SNMP configuration is available.");
            return;
        }

        System.out.println("Default configuration (not indexed, cannot be deleted):");
        final String defaults = parameters(config);
        System.out.println("  " + (defaults.isEmpty() ? "(none)" : defaults));
        System.out.println();

        final List<Definition> definitions = config.getDefinitions();
        if (definitions.isEmpty()) {
            System.out.println("There are no SNMP configuration definitions.");
            return;
        }

        System.out.println("Definitions:");
        int index = 0;
        for (final Definition definition : definitions) {
            index++;
            System.out.printf("  [%d] location=%s", index, location(definition));
            if (definition.getProfileLabel() != null) {
                System.out.printf(" profile-label=%s", definition.getProfileLabel());
            }
            System.out.println();

            final String params = parameters(definition);
            System.out.println("      parameters: " + (params.isEmpty() ? "(none, inherits the defaults)" : params));

            for (final String specific : definition.getSpecifics()) {
                System.out.printf("      specific  %s%n", specific);
            }
            for (final Range range : definition.getRanges()) {
                System.out.printf("      range     %s-%s%n", range.getBegin(), range.getEnd());
            }
            for (final String ipMatch : definition.getIpMatches()) {
                System.out.printf("      ip-match  %s%n", ipMatch);
            }
            if (entryCount(definition) == 0) {
                System.out.println("      (no address entries)");
            }
        }

        System.out.println();
        System.out.println("Delete a definition with '-d <index>' (e.g. '-d 1'). Indexes are positional and change");
        System.out.println("whenever the configuration changes, so re-run --show before every delete.");
    }

    /**
     * Renders the SNMP parameters that are explicitly set on the given configuration,
     * skipping anything left to the defaults. Credentials are masked; use
     * 'opennms:snmp-show-config' to inspect the effective credentials of an agent.
     */
    private static String parameters(final Configuration configuration) {
        final StringBuilder sb = new StringBuilder();
        append(sb, "version", configuration.getVersion());
        append(sb, "port", configuration.getPort());
        append(sb, "timeout", configuration.getTimeout());
        append(sb, "retry", configuration.getRetry());
        append(sb, "read-community", mask(configuration.getReadCommunity()));
        append(sb, "write-community", mask(configuration.getWriteCommunity()));
        append(sb, "max-vars-per-pdu", configuration.getMaxVarsPerPdu());
        append(sb, "max-repetitions", configuration.getMaxRepetitions());
        append(sb, "max-request-size", configuration.getMaxRequestSize());
        append(sb, "proxy-host", configuration.getProxyHost());
        append(sb, "ttl", configuration.getTTL());
        append(sb, "security-name", configuration.getSecurityName());
        append(sb, "security-level", configuration.getSecurityLevel());
        append(sb, "auth-protocol", configuration.getAuthProtocol());
        append(sb, "auth-passphrase", mask(configuration.getAuthPassphrase()));
        append(sb, "privacy-protocol", configuration.getPrivacyProtocol());
        append(sb, "privacy-passphrase", mask(configuration.getPrivacyPassphrase()));
        append(sb, "context-name", configuration.getContextName());
        append(sb, "context-engine-id", configuration.getContextEngineId());
        append(sb, "engine-id", configuration.getEngineId());
        append(sb, "enterprise-id", configuration.getEnterpriseId());
        return sb.toString();
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
        return value == null ? null : MASKED;
    }

    // --------------------------------------------------------------- helpers

    private static int entryCount(final Definition definition) {
        return definition.getSpecifics().size() + definition.getRanges().size() + definition.getIpMatches().size();
    }

    /**
     * Human readable summary of the addresses a definition applies to, used when
     * reporting what was deleted.
     */
    private static String describeEntries(final Definition definition) {
        final StringBuilder sb = new StringBuilder();
        for (final String specific : definition.getSpecifics()) {
            append(sb, "specific", "'" + specific + "'");
        }
        for (final Range range : definition.getRanges()) {
            append(sb, "range", "'" + range.getBegin() + "-" + range.getEnd() + "'");
        }
        for (final String ipMatch : definition.getIpMatches()) {
            append(sb, "ip-match", "'" + ipMatch + "'");
        }
        return sb.length() == 0 ? "no address entries" : sb.toString();
    }

    private static String location(final Definition definition) {
        final String location = definition.getLocation();
        return (location == null || location.trim().isEmpty()) ? DEFAULT_LOCATION : location;
    }
}