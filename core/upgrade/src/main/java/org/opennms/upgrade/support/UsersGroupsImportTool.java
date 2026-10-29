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
package org.opennms.upgrade.support;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.password.DatabasePasswords;
import org.opennms.core.password.UserMgmtStoreSetting;
import org.opennms.netmgt.config.api.UserMgmtStore;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Groupinfo;
import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.config.users.Userinfo;
import org.opennms.upgrade.implementations.UsersGroupsXmlImport;
import org.opennms.upgrade.implementations.UsersGroupsXmlImport.Existing;

/**
 * {@code bin/import-users-groups}: imports a users.xml, a groups.xml and a security-roles.properties into the user
 * management tables, also while OpenNMS is running. Entries that exist already are skipped, or with
 * {@code --overwrite} replaced after one confirmation. The whole import is one transaction under the
 * usermgmt_version locks; it is rolled back when it would leave no administrator, and always with
 * {@code --dry-run}.
 */
public final class UsersGroupsImportTool {

    /** Asks the admin to confirm the overwrite; tests answer it themselves. */
    public interface Prompt {
        /** @return the answer, or null when there is no console */
        String ask(String question);
    }

    private static final int OK = 0;
    private static final int ERROR = 1;
    private static final int BAD_ARGUMENTS = 2;
    private static final int NOT_CONFIRMED = 3;

    private static final String USAGE = String.join("\n",
            "Usage: import-users-groups [--users FILE] [--groups FILE] [--security-roles FILE]",
            "                           [--overwrite] [--quiet] [--dry-run] [--no-decode] [--help]",
            "",
            "Imports users, groups, on-call roles and custom security roles into the database. OpenNMS can be running.",
            "",
            "  --users FILE           a users.xml",
            "  --groups FILE          a groups.xml, with its groups and on-call roles",
            "  --security-roles FILE  a security-roles.properties",
            "  --overwrite            replace the users, groups and on-call roles that exist already, after one",
            "                         confirmation; without it, they are skipped and listed",
            "  --quiet                overwrite without asking (for scripts)",
            "  --dry-run              run the import and roll it back: shows what it would do, writes nothing",
            "  --no-decode            import full names, contact info and service providers as they are, for a",
            "                         users.xml written by OpenNMS 37 or later with " + UserMgmtStore.PROPERTY + "=" + UserMgmtStore.XML,
            "  --help                 print this help",
            "",
            "Exit codes: 0 the import (or dry run) ran, also when it skipped entries; 1 an error, nothing was written;",
            "2 bad arguments; 3 the overwrite was not confirmed.");

    private final Supplier<DataSource> m_dataSource;
    private final File m_opennmsHome;
    private final Prompt m_prompt;
    private final PrintStream m_out;
    private final PrintStream m_err;

    /** The command line. */
    private static final class Arguments {
        private File m_users;
        private File m_groups;
        private File m_securityRoles;
        private boolean m_overwrite;
        private boolean m_quiet;
        private boolean m_dryRun;
        private boolean m_decode = true;
        private boolean m_help;

        /** @throws IllegalArgumentException with the problem, for the usage message */
        private static Arguments parse(final String... args) {
            final Arguments arguments = new Arguments();
            for (int i = 0; i < args.length; i++) {
                final String arg = args[i];
                switch (arg) {
                    case "--users":
                        arguments.m_users = file(arguments.m_users, args, ++i, arg);
                        break;
                    case "--groups":
                        arguments.m_groups = file(arguments.m_groups, args, ++i, arg);
                        break;
                    case "--security-roles":
                        arguments.m_securityRoles = file(arguments.m_securityRoles, args, ++i, arg);
                        break;
                    case "--overwrite":
                        arguments.m_overwrite = true;
                        break;
                    case "--quiet":
                        arguments.m_quiet = true;
                        break;
                    case "--dry-run":
                        arguments.m_dryRun = true;
                        break;
                    case "--no-decode":
                        arguments.m_decode = false;
                        break;
                    case "--help":
                        arguments.m_help = true;
                        return arguments;
                    default:
                        throw new IllegalArgumentException("Unknown argument: " + arg);
                }
            }
            if (arguments.files().isEmpty()) {
                throw new IllegalArgumentException("Give at least one of --users, --groups and --security-roles.");
            }
            return arguments;
        }

        private static File file(final File previous, final String[] args, final int index, final String option) {
            if (previous != null) {
                throw new IllegalArgumentException(option + " is given more than once.");
            }
            // an option where the file should be is a forgotten file
            if (index >= args.length || args[index].startsWith("--")) {
                throw new IllegalArgumentException(option + " needs a FILE.");
            }
            return new File(args[index]);
        }

        private List<File> files() {
            final List<File> files = new ArrayList<>();
            for (final File file : new File[] { m_securityRoles, m_users, m_groups }) {
                if (file != null) {
                    files.add(file);
                }
            }
            return files;
        }
    }

    /** The parsed files; a list is empty when its file is not given. */
    private static final class Input {
        private List<String> m_securityRoles = Collections.emptyList();
        private List<User> m_users = Collections.emptyList();
        private List<Group> m_groups = Collections.emptyList();
        private List<Role> m_onCallRoles = Collections.emptyList();
    }

    /**
     * @param dataSource the OpenNMS database; not used before the arguments, the files and the store setting are checked
     * @param opennmsHome where etc/opennms.properties says which store holds the users; null if unknown
     */
    public UsersGroupsImportTool(final DataSource dataSource, final File opennmsHome, final Prompt prompt, final PrintStream out, final PrintStream err) {
        this(() -> dataSource, opennmsHome, prompt, out, err);
    }

    private UsersGroupsImportTool(final Supplier<DataSource> dataSource, final File opennmsHome, final Prompt prompt, final PrintStream out, final PrintStream err) {
        m_dataSource = dataSource;
        m_opennmsHome = opennmsHome;
        m_prompt = prompt;
        m_out = out;
        m_err = err;
    }

    /** Started by {@code bin/import-users-groups} through opennms_bootstrap.jar, which sets opennms.home. */
    public static void main(final String[] args) {
        final String home = System.getProperty("opennms.home");
        final Console console = System.console();
        final Prompt prompt = question -> {
            if (console == null) {
                return null;
            }
            final String answer = console.readLine("%s ", question);
            // the end of the input is no
            return answer == null ? "" : answer;
        };
        // the data source is set up on first use, so that --help and bad arguments work without a database
        final UsersGroupsImportTool tool = new UsersGroupsImportTool(DataSourceFactory::getInstance, home == null ? null : new File(home), prompt, System.out, System.err);
        System.exit(tool.run(args));
    }

    /** @return the exit code: 0 ran, 1 error (nothing written), 2 bad arguments, 3 overwrite not confirmed */
    public int run(final String... args) {
        final Arguments arguments;
        try {
            arguments = Arguments.parse(args);
        } catch (final IllegalArgumentException e) {
            m_err.println(e.getMessage());
            m_err.println(USAGE);
            return BAD_ARGUMENTS;
        }
        if (arguments.m_help) {
            m_out.println(USAGE);
            return OK;
        }
        for (final File file : arguments.files()) {
            if (!file.isFile() || !file.canRead()) {
                m_err.println("Cannot read " + file + ": it does not exist or is not a readable file.");
                return ERROR;
            }
        }
        if (!isDatabaseStore()) {
            return ERROR;
        }
        final Input input = read(arguments);
        if (input == null) {
            return ERROR;
        }

        Map<String, List<String>> confirmed = null;
        if (arguments.m_overwrite && !arguments.m_quiet && !arguments.m_dryRun) {
            try (final Connection connection = m_dataSource.get().getConnection()) {
                confirmed = UsersGroupsXmlImport.findExisting(connection, input.m_users, input.m_groups, input.m_onCallRoles);
            } catch (final SQLException | RuntimeException e) {
                m_err.println("Cannot read the database: " + message(e));
                return ERROR;
            }
            if (!confirmed.isEmpty() && !confirm(confirmed)) {
                return NOT_CONFIRMED;
            }
        }
        return importFiles(arguments, input, confirmed);
    }

    private boolean isDatabaseStore() {
        if (m_opennmsHome == null) {
            m_err.println("opennms.home is not set; start the tool with bin/import-users-groups.");
            return false;
        }
        try {
            if (UserMgmtStoreSetting.isDatabase(m_opennmsHome)) {
                return true;
            }
            m_err.println("This OpenNMS keeps its users and groups in users.xml and groups.xml (" + UserMgmtStore.PROPERTY + "="
                    + UserMgmtStore.XML + "); the tool imports into the database only.");
        } catch (final IllegalStateException e) {
            m_err.println(e.getMessage());
        }
        return false;
    }

    /** @return the files, or null after printing why one cannot be read */
    private Input read(final Arguments arguments) {
        final Input input = new Input();
        File file = null;
        try {
            file = arguments.m_securityRoles;
            if (file != null) {
                input.m_securityRoles = UsersGroupsXmlImport.readSecurityRoles(file);
            }
            file = arguments.m_users;
            if (file != null) {
                final Userinfo userinfo = UsersGroupsXmlImport.read(Userinfo.class, file);
                input.m_users = userinfo == null ? Collections.emptyList() : userinfo.getUsers();
            }
            file = arguments.m_groups;
            if (file != null) {
                final Groupinfo groupinfo = UsersGroupsXmlImport.read(Groupinfo.class, file);
                input.m_groups = groupinfo == null ? Collections.emptyList() : groupinfo.getGroups();
                input.m_onCallRoles = groupinfo == null ? Collections.emptyList() : groupinfo.getRoles();
            }
            return input;
        } catch (final IOException | RuntimeException e) {
            m_err.println("Cannot read " + file + ": " + message(e));
            return null;
        }
    }

    /** Lists the existing entries and asks once; prints why when the answer is not yes. */
    private boolean confirm(final Map<String, List<String>> existing) {
        final StringBuilder question = new StringBuilder("These entries of the files exist in the database already:\n");
        int count = 0;
        for (final Map.Entry<String, List<String>> entry : existing.entrySet()) {
            question.append("  ").append(entry.getKey()).append(": ").append(String.join(", ", entry.getValue())).append('\n');
            count += entry.getValue().size();
        }
        question.append("Overwrite these ").append(count).append(" existing entries? [y/N]");
        final String answer = m_prompt.ask(question.toString());
        if (answer == null) {
            m_err.println("There is no console to confirm the overwrite, so nothing was written. "
                    + "Add --quiet to overwrite without asking, for example in a script.");
            return false;
        }
        final String trimmed = answer.trim();
        if (trimmed.equalsIgnoreCase("y") || trimmed.equalsIgnoreCase("yes")) {
            return true;
        }
        m_out.println("Not confirmed, so nothing was written.");
        return false;
    }

    /** @param confirmed the existing entries that the admin confirmed to overwrite, or null if nobody was asked */
    private int importFiles(final Arguments arguments, final Input input, final Map<String, List<String>> confirmed) {
        if (arguments.m_dryRun) {
            m_out.println("Dry run: the import runs in a transaction that is rolled back, so nothing is written.");
        }
        try (final Connection connection = m_dataSource.get().getConnection()) {
            connection.setAutoCommit(false);
            final int exitCode;
            try {
                exitCode = importInTransaction(connection, arguments, input, confirmed);
            } catch (final SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (final SQLException rollbackException) {
                    // keep the cause of the failure; the connection may be gone
                    e.addSuppressed(rollbackException);
                }
                throw e;
            }
            // not in a finally block, where a failure on a lost connection would hide the real error
            connection.setAutoCommit(true);
            return exitCode;
        } catch (final SQLException | RuntimeException e) {
            m_err.println("The import failed, so nothing was written: " + message(e));
            return ERROR;
        }
    }

    /** Ends the transaction: commits, or rolls back for a dry run and when a check fails. */
    private int importInTransaction(final Connection connection, final Arguments arguments, final Input input, final Map<String, List<String>> confirmed) throws SQLException {
        // before the import reads anything: the managers and the REST API wait until the transaction ends, and
        // the import and its administrator check see the data that it writes over
        DatabasePasswords.lockAll(connection);
        final UsersGroupsXmlImport xmlImport = new UsersGroupsXmlImport(connection, arguments.m_overwrite ? Existing.OVERWRITE : Existing.SKIP,
                arguments.m_decode, m_out::println);
        xmlImport.securityRoles(input.m_securityRoles);
        xmlImport.users(input.m_users);
        xmlImport.groups(input.m_groups);
        xmlImport.onCallRoles(input.m_onCallRoles);

        if (confirmed != null) {
            // created since the question: nothing is overwritten that the admin did not see
            final Map<String, List<String>> unconfirmed = notIn(xmlImport.existingEntries(), confirmed);
            if (!unconfirmed.isEmpty()) {
                connection.rollback();
                m_err.println("These entries were created after the confirmation, and were not confirmed to be overwritten:");
                printEntries(m_err, unconfirmed);
                m_err.println("Nothing was written. Run the import again.");
                return ERROR;
            }
        }
        // once, after all writes: it reads the final state, and its warning is counted in the summary
        final List<String> problems = xmlImport.administratorProblems();
        if (!problems.isEmpty()) {
            connection.rollback();
            problems.forEach(m_err::println);
            m_err.println("Nothing was written.");
            return ERROR;
        }
        if (arguments.m_dryRun) {
            connection.rollback();
        } else {
            connection.commit();
        }

        if (!xmlImport.existingEntries().isEmpty()) {
            m_out.println("Existing entries " + (arguments.m_dryRun ? "that would be " : "") + (arguments.m_overwrite ? "overwritten:" : "skipped:"));
            printEntries(m_out, xmlImport.existingEntries());
        }
        m_out.println(arguments.m_dryRun ? "Summary of what the import would do:" : "Summary:");
        xmlImport.logSummary();
        m_out.println(arguments.m_dryRun ? "Dry run: rolled back, nothing was written." : "The import was committed.");
        return OK;
    }

    /** @return the entries of {@code entries} that are not in {@code known}, by kind */
    private static Map<String, List<String>> notIn(final Map<String, List<String>> entries, final Map<String, List<String>> known) {
        final Map<String, List<String>> missing = new LinkedHashMap<>();
        for (final Map.Entry<String, List<String>> entry : entries.entrySet()) {
            final List<String> knownNames = known.getOrDefault(entry.getKey(), Collections.emptyList());
            for (final String name : entry.getValue()) {
                if (!knownNames.contains(name)) {
                    missing.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(name);
                }
            }
        }
        return missing;
    }

    private static void printEntries(final PrintStream stream, final Map<String, List<String>> entries) {
        for (final Map.Entry<String, List<String>> entry : entries.entrySet()) {
            stream.println("  " + entry.getKey() + ": " + String.join(", ", entry.getValue()));
        }
    }

    private static String message(final Exception e) {
        return e.getMessage() == null ? e.toString() : e.getMessage();
    }
}
