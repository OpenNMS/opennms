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

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.Range;
import org.opennms.netmgt.snmp.InetAddrUtils;

/**
 * Adds (merges) an SNMP configuration definition into snmp-config.xml.
 *
 * This mirrors the non-deprecated v2 REST endpoint
 * (PUT /opennms/rest/v2/snmp-config/definition), which builds a
 * {@link Definition} and calls {@link SnmpAgentConfigFactory#saveDefinition(Definition, boolean)}.
 */
@Command(scope = "opennms", name = "snmp-add-config",
        description = "Add an SNMP configuration definition to snmp-config.xml. "
                + "A definition must target at least one specific IP, IP range, or IP-match expression. "
                + "IP-match expressions cannot be combined with specific IPs or ranges.")
@Service
public class SnmpAddConfigCommand implements Action {

    private static final String DEFAULT_LOCATION = "Default";

    // The REST layer emits these exact messages; keep them identical for consistency.
    private static final String MISSING_CONTENTS_MESSAGE =
            "Definition must have at least one specific IP, IP range or IP match specified.";
    private static final String CANNOT_MIX_RANGE_AND_IPMATCH_MESSAGE =
            "Cannot have an IP match expression along with IP ranges or specific IP addresses.";

    @Reference
    private SnmpAgentConfigFactory snmpAgentConfigFactory;

    // ---- Address scoping (at least one of specific / range / ip-match required) ----
    @Option(name = "-s", aliases = "--specific", multiValued = true,
            description = "A specific IP address the definition applies to. Repeatable, e.g. '-s 10.1.1.1 -s 10.1.1.2'.")
    private List<String> specifics = new ArrayList<>();

    @Option(name = "-r", aliases = "--range", multiValued = true,
            description = "An IP address range 'begin-end' the definition applies to. Repeatable, e.g. '-r 10.1.1.1-10.1.1.254'.")
    private List<String> ranges = new ArrayList<>();

    @Option(name = "-m", aliases = "--ip-match", multiValued = true,
            description = "An IPLIKE match expression the definition applies to, e.g. '-m 10.1.1.*'. "
                    + "Cannot be combined with -s/--specific or -r/--range.")
    private List<String> ipMatches = new ArrayList<>();

    @Option(name = "-l", aliases = "--location", description = "Monitoring location (default: 'Default').")
    private String location = DEFAULT_LOCATION;

    @Option(name = "-L", aliases = "--profile-label", description = "SNMP profile label to associate with this definition.")
    private String profileLabel;

    // ---- Common SNMP parameters (v1/v2c/v3) ----
    @Option(name = "-v", aliases = "--version", description = "SNMP version: v1, v2c or v3.")
    private String version;

    @Option(name = "-p", aliases = "--port", description = "UDP port (default: 161).")
    private Integer port;

    @Option(name = "--timeout", description = "Timeout in milliseconds.")
    private Integer timeout;

    @Option(name = "--retry", description = "Number of retries.")
    private Integer retry;

    @Option(name = "--read-community", description = "Read community string (v1/v2c).")
    private String readCommunity;

    @Option(name = "--write-community", description = "Write community string (v1/v2c).")
    private String writeCommunity;

    @Option(name = "--max-vars-per-pdu", description = "Maximum variables per PDU (default: 10).")
    private Integer maxVarsPerPdu;

    @Option(name = "--max-repetitions", description = "Maximum repetitions per get-bulk (default: 2).")
    private Integer maxRepetitions;

    @Option(name = "--max-request-size", description = "Maximum request size in bytes (default: 65535, min 484).")
    private Integer maxRequestSize;

    @Option(name = "--proxy-host", description = "Proxy host used to reach the agent.")
    private String proxyHost;

    @Option(name = "--ttl", description = "Time-to-live in milliseconds for config lookups.")
    private Long ttl;

    // ---- SNMPv3 parameters ----
    @Option(name = "--security-name", description = "SNMPv3 security (user) name.")
    private String securityName;

    @Option(name = "--security-level", description = "SNMPv3 security level: 1 (noAuthNoPriv), 2 (authNoPriv), 3 (authPriv).")
    private Integer securityLevel;

    @Option(name = "--auth-protocol", description = "SNMPv3 authentication protocol, e.g. MD5 or SHA.")
    private String authProtocol;

    @Option(name = "--auth-passphrase", description = "SNMPv3 authentication passphrase.")
    private String authPassphrase;

    @Option(name = "--privacy-protocol", description = "SNMPv3 privacy protocol, e.g. DES or AES.")
    private String privacyProtocol;

    @Option(name = "--privacy-passphrase", description = "SNMPv3 privacy passphrase.")
    private String privacyPassphrase;

    @Option(name = "--context-name", description = "SNMPv3 context name.")
    private String contextName;

    @Option(name = "--context-engine-id", description = "SNMPv3 context engine ID.")
    private String contextEngineId;

    @Option(name = "--engine-id", description = "SNMPv3 engine ID.")
    private String engineId;

    @Option(name = "--enterprise-id", description = "SNMPv3 enterprise ID.")
    private String enterpriseId;

    @Override
    public Object execute() {
        final Definition definition = new Definition();

        // Address scoping
        if (specifics != null) {
            for (final String specific : specifics) {
                definition.addSpecific(specific.trim());
            }
        }
        if (ranges != null) {
            for (final String range : ranges) {
                final Range parsed = parseRange(range);
                if (parsed == null) {
                    return null; // parseRange already printed the error
                }
                definition.addRange(parsed);
            }
        }
        if (ipMatches != null) {
            for (final String ipMatch : ipMatches) {
                definition.addIpMatch(ipMatch.trim());
            }
        }

        definition.setLocation((location == null || location.trim().isEmpty()) ? DEFAULT_LOCATION : location.trim());
        if (profileLabel != null) {
            definition.setProfileLabel(profileLabel);
        }

        // Common SNMP parameters
        if (version != null) {
            definition.setVersion(version);
        }
        if (port != null) {
            definition.setPort(port);
        }
        if (timeout != null) {
            definition.setTimeout(timeout);
        }
        if (retry != null) {
            definition.setRetry(retry);
        }
        if (readCommunity != null) {
            definition.setReadCommunity(readCommunity);
        }
        if (writeCommunity != null) {
            definition.setWriteCommunity(writeCommunity);
        }
        if (maxVarsPerPdu != null) {
            definition.setMaxVarsPerPdu(maxVarsPerPdu);
        }
        if (maxRepetitions != null) {
            definition.setMaxRepetitions(maxRepetitions);
        }
        if (maxRequestSize != null) {
            definition.setMaxRequestSize(maxRequestSize);
        }
        if (proxyHost != null) {
            definition.setProxyHost(proxyHost);
        }
        if (ttl != null) {
            definition.setTTL(ttl);
        }

        // SNMPv3 parameters
        if (securityName != null) {
            definition.setSecurityName(securityName);
        }
        if (securityLevel != null) {
            definition.setSecurityLevel(securityLevel);
        }
        if (authProtocol != null) {
            definition.setAuthProtocol(authProtocol);
        }
        if (authPassphrase != null) {
            definition.setAuthPassphrase(authPassphrase);
        }
        if (privacyProtocol != null) {
            definition.setPrivacyProtocol(privacyProtocol);
        }
        if (privacyPassphrase != null) {
            definition.setPrivacyPassphrase(privacyPassphrase);
        }
        if (contextName != null) {
            definition.setContextName(contextName);
        }
        if (contextEngineId != null) {
            definition.setContextEngineId(contextEngineId);
        }
        if (engineId != null) {
            definition.setEngineId(engineId);
        }
        if (enterpriseId != null) {
            definition.setEnterpriseId(enterpriseId);
        }

        // Validate the same way the v2 REST endpoint does before persisting.
        final String validationError = validate(definition);
        if (validationError != null) {
            System.out.println("Error: " + validationError);
            return null;
        }

        try {
            // 'true' persists the merged configuration to snmp-config.xml.
            snmpAgentConfigFactory.saveDefinition(definition, true);
            System.out.println("Successfully added SNMP configuration definition.");
            System.out.println(definition.toString());
        } catch (final Exception e) {
            System.out.println("Failed to add SNMP configuration definition!");
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * Parses a 'begin-end' range string into a {@link Range}, validating both endpoints
     * as IP address literals. Prints an error and returns null on failure.
     */
    private Range parseRange(final String range) {
        final String value = range == null ? "" : range.trim();
        final int dash = value.indexOf('-');
        if (dash <= 0 || dash == value.length() - 1) {
            System.out.println("Error: Invalid range '" + range + "'. Expected format 'begin-end', e.g. '10.1.1.1-10.1.1.254'.");
            return null;
        }
        final String begin = value.substring(0, dash).trim();
        final String end = value.substring(dash + 1).trim();
        if (!isValidInetAddress(begin)) {
            System.out.println("Error: The range begin IP address '" + begin + "' was invalid.");
            return null;
        }
        if (!isValidInetAddress(end)) {
            System.out.println("Error: The range end IP address '" + end + "' was invalid.");
            return null;
        }
        return new Range(begin, end);
    }

    /**
     * Replicates the v2 REST validation semantics: a definition must have at least one
     * of specific / range / ip-match, ip-match cannot be mixed with specifics or ranges,
     * and any specific IP addresses must be valid literals.
     */
    private String validate(final Definition definition) {
        final boolean hasSpecifics = !definition.getSpecifics().isEmpty();
        final boolean hasRanges = !definition.getRanges().isEmpty();
        final boolean hasIpMatches = !definition.getIpMatches().isEmpty();

        if (!hasSpecifics && !hasRanges && !hasIpMatches) {
            return MISSING_CONTENTS_MESSAGE;
        }
        if (hasIpMatches && (hasSpecifics || hasRanges)) {
            return CANNOT_MIX_RANGE_AND_IPMATCH_MESSAGE;
        }
        for (final String specific : definition.getSpecifics()) {
            if (!isValidInetAddress(specific)) {
                return "The specific IP address '" + specific + "' was invalid.";
            }
        }
        if (securityLevel != null && (securityLevel < 1 || securityLevel > 3)) {
            return "Invalid security level '" + securityLevel + "'. Valid values are 1, 2 or 3.";
        }
        return null;
    }

    private static boolean isValidInetAddress(final String address) {
        if (address == null || address.trim().isEmpty()) {
            return false;
        }
        try {
            return InetAddrUtils.addr(address.trim()) != null;
        } catch (final RuntimeException e) {
            return false;
        }
    }
}