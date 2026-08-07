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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.config.trapd.Snmpv3User;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;
import org.opennms.netmgt.dao.api.TrapdConfigDao;

/**
 * Adds an SNMPv3 user to the trapd configuration.
 *
 * SNMPv3 users are the only repeatable entries in trapd-configuration; the remaining
 * settings (trap port, threads, batching...) are scalars and are left untouched here.
 *
 * This mirrors the validation the v2 REST endpoint (PUT /opennms/rest/v2/trapd-configuration)
 * applies to each SNMPv3 user before persisting via {@link TrapdConfigDao#replaceConfig}.
 * Security names are deliberately not required to be unique: the same name can map to
 * distinct credential sets, which is why each user carries a server-assigned id.
 */
@Command(scope = "opennms", name = "trapd-add-config",
        description = "Add an SNMPv3 user to the trapd configuration. "
                + "The user is appended to trapd-configuration and assigned a server-generated id.")
@Service
public class TrapdAddConfigCommand implements Action {

    // The REST layer accepts exactly these; keep the lists identical so the shell and the
    // web UI cannot disagree about what trapd will start with.
    private static final Set<String> AUTH_PROTOCOLS = new HashSet<>(Arrays.asList("MD5", "SHA", "SHA-224", "SHA-256", "SHA-512"));
    private static final Set<String> PRIVACY_PROTOCOLS = new HashSet<>(Arrays.asList("DES", "AES", "AES192", "AES256"));
    private static final int MIN_PASSPHRASE_BYTES = 8;
    private static final String MASKED = "******";

    @Reference
    private TrapdConfigDao trapdConfigDao;

    @Argument(index = 0, name = "securityName", required = true,
            description = "SNMPv3 security (user) name. Need not be unique: the same name may be "
                    + "configured more than once with different credentials.")
    private String securityName;

    @Option(name = "-l", aliases = "--security-level",
            description = "SNMPv3 security level: 1 (noAuthNoPriv), 2 (authNoPriv), 3 (authPriv).")
    private Integer securityLevel;

    @Option(name = "-a", aliases = "--auth-protocol",
            description = "Authentication protocol: MD5, SHA, SHA-224, SHA-256 or SHA-512.")
    private String authProtocol;

    @Option(name = "-A", aliases = "--auth-passphrase",
            description = "Authentication passphrase, at least 8 bytes. May be an SCV reference, e.g. '${scv:alias:key}'.")
    private String authPassphrase;

    @Option(name = "-p", aliases = "--privacy-protocol",
            description = "Privacy protocol: DES, AES, AES192 or AES256.")
    private String privacyProtocol;

    @Option(name = "-P", aliases = "--privacy-passphrase",
            description = "Privacy passphrase, at least 8 bytes. May be an SCV reference, e.g. '${scv:alias:key}'.")
    private String privacyPassphrase;

    @Option(name = "-e", aliases = "--engine-id",
            description = "Engine ID this user is scoped to.")
    private String engineId;

    @Override
    public Object execute() {
        final TrapdConfiguration config = trapdConfigDao.getConfig();
        if (config == null) {
            System.out.println("Error: No trapd configuration is available.");
            return null;
        }

        final Snmpv3User user = new Snmpv3User();
        user.setSecurityName(trimToNull(securityName));
        if (securityLevel != null) {
            user.setSecurityLevel(securityLevel);
        }
        if (authProtocol != null) {
            user.setAuthProtocol(trimToNull(authProtocol));
        }
        if (authPassphrase != null) {
            user.setAuthPassphrase(authPassphrase);
        }
        if (privacyProtocol != null) {
            user.setPrivacyProtocol(trimToNull(privacyProtocol));
        }
        if (privacyPassphrase != null) {
            user.setPrivacyPassphrase(privacyPassphrase);
        }
        if (engineId != null) {
            user.setEngineId(trimToNull(engineId));
        }

        final String validationError = validate(user);
        if (validationError != null) {
            System.out.println("Error: " + validationError);
            return null;
        }

        config.addSnmpv3User(user);

        try {
            // replaceConfig() stamps an id onto the new user before persisting.
            trapdConfigDao.replaceConfig(config);
        } catch (final Exception e) {
            // getConfig() hands back the DAO's cached instance, so roll the addition back:
            // otherwise a failed save would leave the user visible in memory until the next reload.
            config.removeSnmpv3User(user);
            System.out.println("Failed to add SNMPv3 user to the trapd configuration!");
            System.out.println(e.getMessage());
            return null;
        }

        System.out.println("Successfully added SNMPv3 user to the trapd configuration:");
        System.out.println("  id: " + user.getId());
        System.out.println("  " + describe(user));
        return null;
    }

    /**
     * Replicates the per-user validation of the v2 REST endpoint: a security name is required,
     * protocols must be supported and paired with a passphrase, passphrases must be long enough
     * for SNMP4J to accept them, and any supplied security level must agree with the credentials.
     */
    private String validate(final Snmpv3User user) {
        if (user.getSecurityName() == null) {
            return "A security name is required.";
        }

        final Integer level = user.getSecurityLevel();
        if (level != null && (level < 1 || level > 3)) {
            return "Invalid security level '" + level + "'. Valid values are 1, 2 or 3.";
        }

        if (user.getAuthProtocol() != null && !AUTH_PROTOCOLS.contains(user.getAuthProtocol())) {
            return "Unsupported authentication protocol '" + user.getAuthProtocol() + "'. Supported protocols are "
                    + String.join(", ", new TreeSet<>(AUTH_PROTOCOLS)) + ".";
        }
        if (user.getPrivacyProtocol() != null && !PRIVACY_PROTOCOLS.contains(user.getPrivacyProtocol())) {
            return "Unsupported privacy protocol '" + user.getPrivacyProtocol() + "'. Supported protocols are "
                    + String.join(", ", new TreeSet<>(PRIVACY_PROTOCOLS)) + ".";
        }

        final boolean hasAuthProtocol = user.getAuthProtocol() != null;
        final boolean hasAuthPassphrase = !isBlank(user.getAuthPassphrase());
        final boolean hasPrivacyProtocol = user.getPrivacyProtocol() != null;
        final boolean hasPrivacyPassphrase = !isBlank(user.getPrivacyPassphrase());

        if (hasAuthProtocol != hasAuthPassphrase) {
            return "--auth-protocol and --auth-passphrase must be provided together.";
        }
        if (hasPrivacyProtocol != hasPrivacyPassphrase) {
            return "--privacy-protocol and --privacy-passphrase must be provided together.";
        }

        // A leading '*' collides with the masking sentinel the REST API and UI use, so trapd
        // rejects it there too; refuse it here rather than let it round-trip into a masked value.
        if (hasAuthPassphrase && user.getAuthPassphrase().startsWith("*")) {
            return "The authentication passphrase must not begin with '*'.";
        }
        if (hasPrivacyPassphrase && user.getPrivacyPassphrase().startsWith("*")) {
            return "The privacy passphrase must not begin with '*'.";
        }

        // SNMP4J rejects short passphrases when it builds the UsmUser, which would leave trapd
        // unable to restart on reload.
        if (hasAuthPassphrase && byteLength(user.getAuthPassphrase()) < MIN_PASSPHRASE_BYTES) {
            return "The authentication passphrase must be at least " + MIN_PASSPHRASE_BYTES + " bytes.";
        }
        if (hasPrivacyPassphrase && byteLength(user.getPrivacyPassphrase()) < MIN_PASSPHRASE_BYTES) {
            return "The privacy passphrase must be at least " + MIN_PASSPHRASE_BYTES + " bytes.";
        }

        if (level != null && level == 1 && (hasAuthProtocol || hasPrivacyProtocol)) {
            return "Security level 1 (noAuthNoPriv) does not allow auth or privacy credentials.";
        }
        if (level != null && level == 2 && (!hasAuthProtocol || hasPrivacyProtocol)) {
            return "Security level 2 (authNoPriv) requires auth credentials and does not allow privacy credentials.";
        }
        if (level != null && level == 3 && (!hasAuthProtocol || !hasPrivacyProtocol)) {
            return "Security level 3 (authPriv) requires both auth and privacy credentials.";
        }

        return null;
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
        if (value == null) {
            return null;
        }
        return value.startsWith("${scv:") ? value : MASKED;
    }

    private static int byteLength(final String value) {
        return value.getBytes(StandardCharsets.UTF_8).length;
    }

    private static boolean isBlank(final String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String trimToNull(final String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.trim();
    }
}