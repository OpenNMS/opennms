/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.install;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.IOUtils;
import org.opennms.bootstrap.Bootstrap;
import org.opennms.core.db.DataSourceConfigurationFactory;
import org.opennms.core.db.install.SimpleDataSource;
import org.opennms.core.logging.Logging;
import org.opennms.core.schema.Migration;
import org.opennms.core.schema.Migrator;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.ProcessExec;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.opennms.netmgt.icmp.Pinger;
import org.opennms.netmgt.icmp.best.BestMatchPingerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.StringUtils;

/*
 * TODO:
 * - Fix all of the XXX items (some coding, some discussion)
 * - Change the Exceptions to something more reasonable
 * - Do exception handling where it makes sense (give users reasonable error messages for common problems)
 * - Javadoc
 */

/**
 * <p>Installer class.</p>
 */
public class Installer {

    private static final Logger LOG = LoggerFactory.getLogger(Installer.class);

    static final String LIBRARY_PROPERTY_FILE = "libraries.properties";

    String m_opennms_home = null;
    boolean m_update_database = false;
    boolean m_update_iplike = false;
    boolean m_do_full_vacuum = false;
    boolean m_do_vacuum = false;
    boolean m_fix_constraint = false;
    boolean m_ignore_database_version = false;
    boolean m_remove_database = false;
    boolean m_skip_upgrade_tools = false;

    String m_etc_dir = "";
    String m_import_dir = null;
    String m_install_servletdir = null;
    String m_library_search_path = null;
    String m_fix_constraint_name = null;
    boolean m_fix_constraint_remove_rows = false;

    protected Options options = new Options();
    protected CommandLine m_commandLine;
    private Migration m_migration = new Migration();
    private Migrator m_migrator = new Migrator();

    Properties m_properties = null;

    String m_required_options = "At least one of -d, -i, -s, -y, -C, or -T is required.";

    private static final String OPENNMS_DATA_SOURCE_NAME = "opennms";

    private static final String ADMIN_DATA_SOURCE_NAME = "opennms-admin";

    /**
     * <p>Constructor for Installer.</p>
     */
    public Installer() {
    }

    /**
     * <p>install</p>
     *
     * @param argv an array of {@link java.lang.String} objects.
     * @throws java.lang.Exception if any.
     */
    public void install(final String[] argv) throws Exception {
        printHeader();
        loadProperties();
        parseArguments(argv);

        final boolean doDatabase = (m_update_database || m_update_iplike);

        GenericApplicationContext context = null;
        if (doDatabase) {
            final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.OPENNMS_DATASOURCE_CONFIG_FILE_NAME);

            InputStream is = new FileInputStream(cfgFile);
            final JdbcDataSource adminDsConfig = new DataSourceConfigurationFactory(is).getJdbcDataSource(ADMIN_DATA_SOURCE_NAME);
            final DataSource adminDs = new SimpleDataSource(adminDsConfig);
            is.close();

            is = new FileInputStream(cfgFile);
            final JdbcDataSource dsConfig = new DataSourceConfigurationFactory(is).getJdbcDataSource(OPENNMS_DATA_SOURCE_NAME);
            final DataSource ds = new SimpleDataSource(dsConfig);
            is.close();

            context = new GenericApplicationContext();
            context.setClassLoader(Bootstrap.loadClasses(new File(m_opennms_home), true));

            Migrator.validateLiquibaseChangelog(context);

            m_migrator.setDataSource(ds);
            m_migrator.setAdminDataSource(adminDs);
            m_migrator.setValidateDatabaseVersion(!m_ignore_database_version);

            m_migration.setDatabaseName(dsConfig.getDatabaseName());
            m_migration.setSchemaName(dsConfig.getSchemaName());
            m_migration.setAdminUser(adminDsConfig.getUserName());
            m_migration.setAdminPassword(adminDsConfig.getPassword());
            m_migration.setDatabaseUser(dsConfig.getUserName());
            m_migration.setDatabasePassword(dsConfig.getPassword());
        }

        checkIPv6();

        /*
         * Make sure we can execute the rrdtool binary when the
         * JniRrdStrategy is enabled.
         */

        boolean using_jni_rrd_strategy = System.getProperty("org.opennms.rrd.strategyClass", "")
                .contains("JniRrdStrategy");

        if (using_jni_rrd_strategy) {
            File rrd_binary = new File(System.getProperty("rrd.binary"));
            if (!rrd_binary.canExecute()) {
                throw new Exception("Cannot execute the rrdtool binary '" + rrd_binary.getAbsolutePath()
                + "' required by the current RRD strategy. Update the rrd.binary field in opennms.properties appropriately.");
            }
        }

        /*
         * make sure we can load the ICMP library before we go any farther
         */

        if (!Boolean.getBoolean("skip-native")) {
            String icmp_path = findLibrary("jicmp", m_library_search_path, false);
            String icmp6_path = findLibrary("jicmp6", m_library_search_path, false);
            String jrrd_path = findLibrary("jrrd", m_library_search_path, false);
            String jrrd2_path = findLibrary("jrrd2", m_library_search_path, false);
            writeLibraryConfig(icmp_path, icmp6_path, jrrd_path, jrrd2_path);
        }

        /*
         * Everything needs to use the administrative data source until we
         * verify that the opennms database is created below (and where we
         * create it if it doesn't already exist).
         */

        if (doDatabase) {
            LOG.info(String.format("* using '%s' as the PostgreSQL user for OpenNMS", m_migration.getAdminUser()));
            LOG.info(String.format("* using '%s' as the PostgreSQL database name for OpenNMS", m_migration.getDatabaseName()));
            if (m_migration.getSchemaName() != null) {
                LOG.info(String.format("* using '%s' as the PostgreSQL schema name for OpenNMS", m_migration.getSchemaName()));
            }

            m_migrator.setupDatabase(m_migration, m_update_database, m_do_vacuum, m_do_vacuum, m_update_iplike, context);

            // XXX why do both options need to be set to remove the database?
            if (m_update_database && m_remove_database) {
                m_migrator.databaseRemoveDB(m_migration);
            }

            if (m_update_database) {
                createConfiguredFile();
            }

            context.close();
        }

        handleConfigurationChanges();

        System.out.println();
        System.out.println("Installer completed successfully!");

        if (!m_skip_upgrade_tools) {
            System.setProperty("opennms.manager.class", "org.opennms.upgrade.support.Upgrade");
            Bootstrap.main(new String[] {});
        }
    }

    private void checkIPv6() {
        final IPv6Validator v6Validator = new IPv6Validator();
        if (!v6Validator.isPlatformIPv6Ready()) {
            System.out.println("Your OS does not support IPv6.");
        }
    }

    private void handleConfigurationChanges() {
        File etcDir = new File(m_opennms_home + File.separator + "etc");
        File importDir = new File(m_import_dir);
        File[] files = etcDir.listFiles(getImportFileFilter());

        if (!importDir.exists()) {
            System.out.print("- Creating imports directory (" + importDir.getAbsolutePath() + "... ");
            if (!importDir.mkdirs()) {
                System.out.println("FAILED");
                System.exit(1);
            }
            System.out.println("OK");
        }

        System.out.print("- Checking for old import files in " + etcDir.getAbsolutePath() + "... ");
        if (files.length > 0) {
            System.out.println("FOUND");
            for (File f : files) {
                String newFileName = f.getName().replace("imports-", "");
                File newFile = new File(importDir, newFileName);
                System.out.print("  - moving " + f.getName() + " to " + importDir.getPath() + "... ");
                if (f.renameTo(newFile)) {
                    System.out.println("OK");
                } else {
                    System.out.println("FAILED");
                }
            }
        } else {
            System.out.println("DONE");
        }
    }

    private FilenameFilter getImportFileFilter() {
        return new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.matches("imports-.*\\.xml");
            }

        };
    }

    /**
     * <p>createConfiguredFile</p>
     *
     * @throws java.io.IOException if any.
     */
    public void createConfiguredFile() throws IOException {
        File f = new File(m_opennms_home + File.separator + "etc" + File.separator + "configured");
        if (!f.createNewFile()) {
            LOG.warn("Could not create file: {}", f.getPath());
        }
    }

    /**
     * <p>printHeader</p>
     */
    public void printHeader() {
        System.out.println("==============================================================================");
        System.out.println("OpenNMS Installer");
        System.out.println("==============================================================================");
        System.out.println("");
        System.out.println("Configures PostgreSQL tables, users, and other miscellaneous settings.");
        System.out.println("");
    }

    /**
     * <p>loadProperties</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void loadProperties() throws Exception {
        m_properties = new Properties();
        m_properties.load(Installer.class.getResourceAsStream("/installer.properties"));

        /*
         * Do this if we want to merge our properties with the system
         * properties...
         */
        final Properties sys = System.getProperties();
        m_properties.putAll(sys);

        m_opennms_home = fetchProperty("install.dir");
        m_etc_dir = fetchProperty("install.etc.dir");

        loadEtcPropertiesFile("opennms.properties");
        // Used to retrieve 'org.opennms.rrd.strategyClass'
        loadEtcPropertiesFile("rrd-configuration.properties");

        m_install_servletdir = fetchProperty("install.servlet.dir");
        try {
            m_import_dir = fetchProperty("importer.requisition.dir");
        } catch (Exception e) {
            m_import_dir = m_opennms_home + File.separator + "etc" + File.separator + "imports";
        }

        //        final String pg_lib_dir = m_properties.getProperty("install.postgresql.dir");
        //
        //        if (pg_lib_dir != null) {
        //            m_installerDb.setPostgresPlPgsqlLocation(pg_lib_dir + File.separator + "plpgsql");
        //            m_installerDb.setPostgresIpLikeLocation(pg_lib_dir + File.separator + "iplike");
        //        }
        //
        //        m_installerDb.setStoredProcedureDirectory(m_etc_dir);
        //        m_installerDb.setCreateSqlLocation(m_etc_dir + File.separator + "create.sql");
    }

    private void loadEtcPropertiesFile(final String propertiesFile) throws IOException {
        try {
            final Properties opennmsProperties = new Properties();
            final InputStream ois = new FileInputStream(m_etc_dir + File.separator + propertiesFile);
            opennmsProperties.load(ois);
            // We only want to put() things that weren't already overridden in installer.properties
            for (final Entry<Object,Object> p : opennmsProperties.entrySet()) {
                if (!m_properties.containsKey(p.getKey())) {
                    m_properties.put(p.getKey(), p.getValue());
                }
            }
        } catch (final FileNotFoundException e) {
            System.out.println("WARNING: unable to load " + m_etc_dir + File.separator + propertiesFile);
        }
    }

    /**
     * <p>fetchProperty</p>
     *
     * @param property a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public String fetchProperty(String property) throws Exception {
        String value;

        if ((value = m_properties.getProperty(property)) == null) {
            throw new Exception("property \"" + property + "\" not set "
                    + "from bundled installer.properties file");
        }

        return value;
    }

    /**
     * <p>parseArguments</p>
     *
     * @param argv an array of {@link java.lang.String} objects.
     * @throws java.lang.Exception if any.
     */
    public void parseArguments(String[] argv) throws Exception {

        options.addOption("h", "help", false, "this help");

        // database-related options
        options.addOption("d", "do-database", false,
                "perform database actions");

        options.addOption("Z", "remove-database", false,
                "remove the OpenNMS database");

        options.addOption("u", "username", true,
                "username of the database account (default: 'opennms')");
        options.addOption("p", "password", true,
                "password of the database account (default: 'opennms')");
        options.addOption("a", "admin-username", true,
                "username of the database administrator (default: 'postgres')");
        options.addOption("A", "admin-password", true,
                "password of the database administrator (default: '')");
        options.addOption("D", "database-url", true,
                "JDBC database URL (default: jdbc:postgresql://localhost:5432/");
        options.addOption("P", "database-name", true,
                "name of the PostgreSQL database (default: opennms)");

        options.addOption("c", "clean-database", false,
                "this option does nothing");
        options.addOption("i", "insert-data", false,
                "(obsolete)");
        options.addOption("s", "stored-procedure", false,
                "add the IPLIKE stored procedure if it's missing");
        options.addOption("v", "vacuum", false,
                "vacuum (optimize) the database");
        options.addOption("f", "vacuum-full", false,
                "vacuum full the database (recovers unused disk space)");
        options.addOption("Q", "ignore-database-version", false,
                "disable the database version check");

        options.addOption("x", "database-debug", false,
                "turn on debugging for the database data transformation");
        options.addOption("e", "extended-repairs", false,
                "enable extended repairs of old schemas");

        // general installation options
        options.addOption("l", "library-path", true,
                "library search path (directories separated by '" + File.pathSeparator + "')");

        // upgrade tools options
        options.addOption("S", "skip-upgrade-tools", false,
                "Skip the execution of the upgrade tools (post-processing tasks)");

        CommandLineParser parser = new PosixParser();
        m_commandLine = parser.parse(options, argv);

        if (m_commandLine.hasOption("h")) {
            usage(options, m_commandLine);
            System.exit(0);
        }

        options.addOption("u", "username", true,
                "replaced by opennms-datasources.xml");
        options.addOption("p", "password", true,
                "replaced by opennms-datasources.xml");
        options.addOption("a", "admin-username", true,
                "replaced by opennms-datasources.xml");
        options.addOption("A", "admin-password", true,
                "replaced by opennms-datasources.xml");
        options.addOption("D", "database-url", true,
                "replaced by opennms-datasources.xml");
        options.addOption("P", "database-name", true,
                "replaced by opennms-datasources.xml");

        if (m_commandLine.hasOption("c")) {
            usage(options, m_commandLine, "The 'c' option was deprecated in 1.6, and disabled in 1.8.  You should backup and then drop the database before running install to reset your data.", null);
            System.exit(1);
        }

        if (m_commandLine.hasOption("u")
                || m_commandLine.hasOption("p")
                || m_commandLine.hasOption("a")
                || m_commandLine.hasOption("A")
                || m_commandLine.hasOption("D")
                || m_commandLine.hasOption("P")) {
            usage(
                    options,
                    m_commandLine,
                    "The 'u', 'p', 'a', 'A', 'D', and 'P' options have all been superceded.\nPlease edit $OPENNMS_HOME/etc/opennms-datasources.xml instead.", null);
            System.exit(1);
        }

        m_fix_constraint = m_commandLine.hasOption("C");
        m_fix_constraint_name = m_commandLine.getOptionValue("C");
        if (m_commandLine.hasOption("e")) {
            System.setProperty("opennms.contexts", "production,repair");
        }
        m_update_database = m_commandLine.hasOption("d");
        m_remove_database = m_commandLine.hasOption("Z");
        m_do_full_vacuum = m_commandLine.hasOption("f");
        m_library_search_path = m_commandLine.getOptionValue("l", m_library_search_path);
        m_ignore_database_version = m_commandLine.hasOption("Q");
        m_update_iplike = m_commandLine.hasOption("s");
        m_do_vacuum = m_commandLine.hasOption("v");
        //m_installerDb.setDebug(m_commandLine.hasOption("x"));
        if (m_commandLine.hasOption("x")) {
            m_migrator.enableDebug();
        }
        m_fix_constraint_remove_rows = m_commandLine.hasOption("X");
        m_skip_upgrade_tools = m_commandLine.hasOption("S");

        if (m_commandLine.getArgList().size() > 0) {
            usage(options, m_commandLine, "Unknown command-line arguments: "
                    + Arrays.toString(m_commandLine.getArgs()), null);
            System.exit(1);
        }

        if (!m_update_database && !m_update_iplike && m_library_search_path == null) {
            usage(options, m_commandLine, "Nothing to do.  Use -h for help.", null);
            System.exit(1);
        }
    }

    private void usage(Options options, CommandLine cmd) {
        usage(options, cmd, null, null);
    }

    private void usage(Options options, CommandLine cmd, String error,
            Exception e) {
        HelpFormatter formatter = new HelpFormatter();
        PrintWriter pw = new PrintWriter(System.out);

        if (error != null) {
            pw.println("An error occurred: " + error + "\n");
        }

        formatter.printHelp("usage: install [options]", options);

        if (e != null) {
            pw.println(e.getMessage());
            e.printStackTrace(pw);
        }

        pw.close();
    }

    /**
     * <p>main</p>
     *
     * @param argv an array of {@link java.lang.String} objects.
     * @throws java.lang.Exception if any.
     */
    public static void main(String[] argv) throws Exception {
        final Map<String,String> mdc = Logging.getCopyOfContextMap();
        Logging.putPrefix("install");
        new Installer().install(argv);
        Logging.setContextMap(mdc);
    }

    /**
     * <p>findLibrary</p>
     *
     * @param libname a {@link java.lang.String} object.
     * @param path a {@link java.lang.String} object.
     * @param isRequired a boolean.
     * @return a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public String findLibrary(String libname, String path, boolean isRequired) throws Exception {
        String fullname = System.mapLibraryName(libname);

        ArrayList<String> searchPaths = new ArrayList<>();

        if (path != null) {
            for (String entry : path.split(File.pathSeparator)) {
                searchPaths.add(entry);
            }
        }

        try {
            File confFile = new File(m_opennms_home + File.separator + "etc" + File.separator + LIBRARY_PROPERTY_FILE);
            final Properties p = new Properties();
            final InputStream is = new FileInputStream(confFile);
            p.load(is);
            is.close();
            for (final Object k : p.keySet()) {
                String key = (String)k;
                if (key.startsWith("opennms.library")) {
                    String value = p.getProperty(key);
                    value = value.replaceAll(File.separator + "[^" + File.separator + "]*$", "");
                    searchPaths.add(value);
                }
            }
        } catch (Throwable e) {
            // ok if we can't read these, we'll try to find them
        }

        if (System.getProperty("java.library.path") != null) {
            for (final String entry : System.getProperty("java.library.path").split(File.pathSeparator)) {
                searchPaths.add(entry);
            }
        }

        if (!System.getProperty("os.name").contains("Windows")) {
            String[] defaults = {
                    "/usr/lib/jni",
                    "/usr/lib",
                    "/usr/local/lib",
                    "/opt/NMSjicmp/lib/32",
                    "/opt/NMSjicmp/lib/64",
                    "/opt/NMSjicmp6/lib/32",
                    "/opt/NMSjicmp6/lib/64"
            };
            for (final String entry : defaults) {
                searchPaths.add(entry);
            }
        }

        System.out.println("- searching for " + fullname + ":");
        for (String dirname : searchPaths) {
            File entry = new File(dirname);

            if (entry.isFile()) {
                // if they specified a file, try the parent directory instead
                dirname = entry.getParent();
            }



            String fullpath = dirname + File.separator + fullname;
            if (loadLibrary(fullpath)) {
                return fullpath;
            }

            if (fullname.endsWith(".dylib")) {
                final String fullPathOldExtension = fullpath.replace(".dylib", ".jnilib");
                if (loadLibrary(fullPathOldExtension)) {
                    return fullPathOldExtension;
                }
            }
        }

        if (isRequired) {
            final StringBuilder buf = new StringBuilder();
            for (final String pathEntry : System.getProperty("java.library.path").split(File.pathSeparator)) {
                buf.append(" ");
                buf.append(pathEntry);
            }

            throw new Exception("Failed to load the required " + libname + " library that is required at runtime.  By default, we search the Java library path:" + buf.toString() + ".  For more information, see http://www.opennms.org/index.php/" + libname);
        } else {
            System.out.println("- Failed to load the optional " + libname + " library.");
            System.out.println("  - This error is not fatal, since " + libname + " is only required for optional features.");
            System.out.println("  - For more information, see http://www.opennms.org/index.php/" + libname);
        }

        return null;
    }

    /**
     * <p>loadLibrary</p>
     *
     * @param path a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean loadLibrary(final String path) {
        try {
            System.out.print("  - trying to load " + path + ": ");
            System.load(path);
            System.out.println("OK");
            return true;
        } catch (final UnsatisfiedLinkError ule) {
            System.out.println("NO");
        }
        return false;
    }

    /**
     * <p>writeLibraryConfig</p>
     *
     * @param jicmp_path a {@link java.lang.String} object.
     * @param jicmp6_path TODO
     * @param jrrd_path a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    public void writeLibraryConfig(final String jicmp_path, final String jicmp6_path, final String jrrd_path, final String jrrd2_path)
            throws IOException {
        Properties libraryProps = new Properties();

        if (jicmp_path != null && jicmp_path.length() != 0) {
            libraryProps.put("opennms.library.jicmp", jicmp_path);
        }

        if (jicmp6_path != null && jicmp6_path.length() != 0) {
            libraryProps.put("opennms.library.jicmp6", jicmp6_path);
        }

        if (jrrd_path != null && jrrd_path.length() != 0) {
            libraryProps.put("opennms.library.jrrd", jrrd_path);
        }

        if (jrrd2_path != null && jrrd2_path.length() != 0) {
            libraryProps.put("opennms.library.jrrd2", jrrd2_path);
        }

        File f = null;
        try {
            f = new File(m_opennms_home + File.separator + "etc" + File.separator + LIBRARY_PROPERTY_FILE);
            if(!f.createNewFile()) {
                LOG.warn("Could not create file: {}", f.getPath());
            }
            FileOutputStream os = new FileOutputStream(f);
            libraryProps.store(os, null);
        } catch (IOException e) {
            System.out.println("unable to write to " + f.getPath());
            throw e;
        }
    }

    /**
     * <p>pingLocalhost</p>
     *
     * @throws java.io.IOException if any.
     */
    public void pingLocalhost() throws Exception {
        String host = "127.0.0.1";

        java.net.InetAddress addr = null;
        try {
            addr = InetAddress.getByName(host);
        } catch (java.net.UnknownHostException e) {
            System.out.println("UnknownHostException when looking up " + host
                    + ".");
            throw e;

        }

        Pinger pinger;
        try {

            pinger = new BestMatchPingerFactory().getInstance();

        } catch (UnsatisfiedLinkError e) {
            System.out.println("UnsatisfiedLinkError while creating an ICMP Pinger.  Most likely failed to load "
                    + "libjicmp.so.  Try setting the property 'opennms.library.jicmp' to point at the "
                    + "full path name of the libjicmp.so shared library or switch to using the JnaPinger "
                    + "(e.g. 'java -Dopennms.library.jicmp=/some/path/libjicmp.so ...')\n"
                    + "You can also set the 'opennms.library.jicmp6' property in the same manner to specify "
                    + "the location of the JICMP6 library.");
            throw e;
        } catch (NoClassDefFoundError e) {
            System.out.println("NoClassDefFoundError while creating an IcmpSocket.  Most likely failed to load libjicmp.so" +
                    "or libjicmp6.so.");
            throw e;
        } catch (Exception e) {
            System.out.println("Exception while creating Pinger.");
            throw e;
        }


        // using regular InetAddress toString here since is just printed for the users benefit
        System.out.print("Pinging " + host + " (" + addr + ")...");

        Number rtt = pinger.ping(addr);

        if (rtt == null) {
            System.out.println("failed.!");
        } else {
            System.out.printf("successful.. round trip time: %.3f ms%n", rtt.doubleValue() / 1000.0);
        }

    }
}
