//
// // This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2005 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code
// and modified
// code that was published under the GNU General Public License. Copyrights
// for
// modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// The code in this file is Copyright (C) 2004 DJ Gregor.
//
// Based on install.pl which was Copyright (C) 1999-2001 Oculan Corp. All
// rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//

package org.opennms.install;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.util.LinkedList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.opennms.core.utils.ProcessExec;
import org.opennms.netmgt.dao.db.InstallerDb;
import org.opennms.netmgt.dao.db.SimpleDataSource;
import org.opennms.protocols.icmp.IcmpSocket;
import org.springframework.util.StringUtils;

/*
 * Big To-dos:
 * - Fix all of the XXX items (some coding, some discussion)
 * - Change the Exceptions to something more reasonable
 * - Do exception handling where it makes sense (give users reasonable error
 * messages for common problems)
 * - Javadoc
 */

public class Installer {
    static final String s_version =
        "$Id$";

    String m_opennms_home = null;

    boolean m_update_database = false;

    boolean m_do_inserts = false;

    boolean m_skip_constraints = false;

    boolean m_update_iplike = false;

    boolean m_update_unicode = false;

    boolean m_install_webapp = false;

    boolean m_fix_constraint = false;

    boolean m_force = false;

    String m_pg_driver = null;

    String m_pg_url = null;

    String m_pg_user = "postgres";

    String m_pg_pass = "";

    String m_tomcat_conf = null;

    String m_webappdir = null;

    String m_install_servletdir = null;

    String m_fix_constraint_name = null;

    boolean m_fix_constraint_remove_rows = false;

    private PrintStream m_out;

    Properties m_properties = null;

    String m_required_options = "At least one of -d, -i, -s, -U, -y, "
            + "-C, or -T is required.";
    
    private InstallerDb m_installerDb = new InstallerDb();
    
    public Installer() {
        setOutputStream(System.out);
    }
    
    public void install(String[] argv) throws Exception {
        printHeader();
        loadProperties();
        parseArguments(argv);

        if (!m_update_database && !m_do_inserts && !m_update_iplike
                && !m_update_unicode && m_tomcat_conf == null
                && !m_install_webapp && !m_fix_constraint) {
            throw new Exception("Nothing to do.\n" + m_required_options
                    + "\nUse '-h' for help.");
        }
        
        DataSource adminDataSource =
            new SimpleDataSource(m_pg_driver, m_pg_url + "template1",
                                    m_pg_user, m_pg_pass);
        m_installerDb.setAdminDataSource(adminDataSource);
        DataSource opennmsDataSource =
            new SimpleDataSource(m_pg_driver, m_pg_url + m_installerDb.getDatabaseName(),
                                    m_pg_user, m_pg_pass);
        m_installerDb.setDataSource(opennmsDataSource);

        /*
         * Everything needs to use the administrative data source until
         * we verify that the opennms database is created below (and where
         * we create it if it doesn't already exist).
         */

        // XXX Check Tomcat version?

        if (m_update_database || m_update_iplike || m_update_unicode
                || m_do_inserts || m_fix_constraint) {
            m_installerDb.databaseCheckVersion();
            m_installerDb.databaseCheckLanguage();
        }

        printDiagnostics();

        verifyFilesAndDirectories();

        if (m_install_webapp) {
            checkWebappOldOpennmsDir();
            checkServerXmlOldOpennmsContext();
        }

        if (m_update_database || m_fix_constraint) {
            m_installerDb.readTables();
        }

        if (m_update_database) {
            // XXX Check and optionally modify pg_hba.conf

            if (!m_installerDb.databaseUserExists()) {
                m_installerDb.databaseAddUser();
            }
            if (!m_installerDb.databaseDBExists()) {
                m_installerDb.databaseAddDB();
            }
        }
        
        // We can now use the opennms database

        if (m_fix_constraint) {
            m_installerDb.fixConstraint(m_fix_constraint_name,
                                        m_fix_constraint_remove_rows);
        }

        if (m_update_database) {
            m_installerDb.checkOldTables();
            if (!m_skip_constraints) {
                m_installerDb.checkConstraints();
                m_installerDb.checkIndexUniqueness();
            }
            m_installerDb.createSequences();

            // should we be using createFunctions and createLanguages instead?
            m_installerDb.updatePlPgsql();

            // should we be using createFunctions instead?
            m_installerDb.addStoredProcedures();

            m_installerDb.addColumnReplacements();
            m_installerDb.createTables();
            m_installerDb.closeColumnReplacements();
            
            //createIndexes();
            // createFunctions(m_cfunctions); // Unused, not in create.sql
            // createLanguages(); // Unused, not in create.sql
            // createFunctions(m_functions); // Unused, not in create.sql

            m_installerDb.fixData();
        }

        if (m_do_inserts) {
            m_installerDb.insertData();
        }

        if (m_update_unicode) {
            m_installerDb.checkUnicode();
        }

        if (m_install_webapp) {
            installWebApp();
        }

        if (m_tomcat_conf != null) {
            updateTomcatConf();
        }

        if (m_update_iplike) {
            m_installerDb.updateIplike();
        }

        m_installerDb.closeConnection();
        m_installerDb.closeAdminConnection();

        if (m_update_database) {
            createConfiguredFile();
        }

        m_out.println();
        m_out.println("Installer completed successfully!");
    }

    public void createConfiguredFile() throws IOException {
        File f = new File(m_opennms_home + File.separator + "etc"
                + File.separator + "configured");
        f.createNewFile();
    }

    public void printHeader() {
        m_out.println("==============================================="
                + "===============================");
        m_out.println("OpenNMS Installer Version " + s_version);
        m_out.println("==============================================="
                + "===============================");
        m_out.println("");
        m_out.println("Configures PostgreSQL tables, users, and other "
                + "miscellaneous settings.");
        m_out.println("");
    }

    public void loadProperties() throws Exception {
        m_properties = new Properties();
        m_properties.load(Installer.class.getResourceAsStream("installer.properties"));

        /*
         * Do this if we want to merge our properties with the system
         * properties...
         */
        Properties sys = System.getProperties();
        m_properties.putAll(sys);

        m_opennms_home = fetchProperty("install.dir");
        m_installerDb.setDatabaseName(fetchProperty("install.database.name"));
        m_installerDb.setPostgresOpennmsUser(fetchProperty("install.database.user"));
        m_installerDb.setPassword(fetchProperty("install.database.password"));
        m_pg_driver = fetchProperty("install.database.driver");
        m_pg_url = fetchProperty("install.database.url");
        m_installerDb.setProgresBinaryDirectory(fetchProperty("install.database.bindir"));
        String etcDirectory = fetchProperty("install.etc.dir");
        m_install_servletdir = fetchProperty("install.servlet.dir");

        String soext = fetchProperty("build.soext");
        String pg_iplike_dir = fetchProperty("install.postgresql.dir");

        if (pg_iplike_dir != null) {
        	m_installerDb.setPgIpLikeLocation(pg_iplike_dir + File.separator + "iplike." + soext);
        }
        
        m_installerDb.setStoredProcedureDirectory(etcDirectory);
        m_installerDb.setCreateSqlLocation(etcDirectory + File.separator + "create.sql");
    }

    public String fetchProperty(String property) throws Exception {
        String value;

        if ((value = m_properties.getProperty(property)) == null) {
            throw new Exception("property \"" + property + "\" not set "
                    + "from bundled installer.properties file");
        }

        return value;
    }

    public void parseArguments(String[] argv) throws Exception {
        LinkedList<String> args = new LinkedList<String>();

        for (int i = 0; i < argv.length; i++) {
            StringBuffer b = new StringBuffer(argv[i]);
            boolean is_arg = false;

            while (b.length() > 0 && b.charAt(0) == '-') {
                is_arg = true;
                b.deleteCharAt(0);
            }

            if (is_arg) {
                while (b.length() > 0) {
                    char c = b.charAt(0);
                    b.deleteCharAt(0);

                    switch (c) {
                    case 'h':
                        printHelp();
                        break;

                    case 'c':
                        m_installerDb.setForce(true);
                        break;

                    case 'C':
                        i++;
                        m_fix_constraint = true;
                        m_fix_constraint_name = getNextArg(argv, i, 'C');
                        break;

                    case 'd':
                        m_update_database = true;
                        break;

                    case 'D':
                        i++;
                    	m_pg_url = getNextArg(argv, i, 'D');
                    	break;
                    	
                    case 'i':
                        m_do_inserts = true;
                        break;

                    case 'n':
                        m_skip_constraints = true;

                    case 'N':
                        m_installerDb.setIgnoreNotNull(true);
                        break;

                    case 'p':
                        i++;
                        m_pg_pass = getNextArg(argv, i, 'p');
                        break;

                    case 'R':
                        m_installerDb.setNoRevert(true);
                        break;

                    case 's':
                        m_update_iplike = true;
                        break;

                    case 'T':
                        i++;
                        m_tomcat_conf = getNextArg(argv, i, 'T');
                        break;

                    case 'u':
                        i++;
                        m_pg_user = getNextArg(argv, i, 'u');
                        m_installerDb.setPostgresAdminUser(m_pg_user);
                        break;

                    case 'U':
                        m_update_unicode = true;
                        break;

                    case 'w':
                        i++;
                        m_webappdir = getNextArg(argv, i, 'w');
                        break;

                    case 'x':
                        m_installerDb.setDebug(true);
                        break;

                    case 'X':
                        m_fix_constraint_remove_rows = true;
                        break;

                    case 'y':
                        m_install_webapp = true;
                        break;

                    default:
                        throw new Exception("unknown option '" + c + "'"
                                + ", use '-h' option for usage");
                    }
                }
            } else {
                args.add(argv[i]);
            }
        }

        if (args.size() != 0) {
            throw new Exception("too many command-line arguments specified");
        }
    }

    public String getNextArg(String[] argv, int i, char letter)
            throws Exception {
        if (i >= argv.length) {
            throw new Exception("no argument provided for '" + letter
                    + "' option");
        }
        if (argv[i].charAt(0) == '-') {
            throw new Exception("argument to '" + letter + "' option looks "
                    + "like another option (begins with a dash): \""
                    + argv[i] + "\"");
        }
        return argv[i];
    }

    public void printDiagnostics() {
        m_out.println("* using '" + m_installerDb.getPostgresOpennmsUser()
                      + "' as the PostgreSQL "
                      + "user for OpenNMS");
        m_out.println("* using '" + m_installerDb.getPassword()
                      + "' as the PostgreSQL "
                      + "password for OpenNMS");
        m_out.println("* using '" + m_installerDb.getDatabaseName()
                      + "' as the PostgreSQL database name for OpenNMS");
    }

    public void verifyFilesAndDirectories() throws FileNotFoundException {
        if (m_update_database) {
            verifyFileExists(true, m_installerDb.getStoredProcedureDirectory(), "SQL directory",
                             "install.etc.dir property");

            verifyFileExists(false, m_installerDb.getCreateSqlLocation(),
                             "create.sql",
                             "install.etc.dir property");
        }

        if (m_tomcat_conf != null) {
            verifyFileExists(
                             false,
                             m_tomcat_conf,
                             "Tomcat startup configuration file tomcat4.conf",
                             "-T option");
        }

        if (m_install_webapp) {
            verifyFileExists(true, m_webappdir, "Tomcat context directory",
                             "-w option");

            verifyFileExists(true, m_install_servletdir,
                             "OpenNMS servlet directory",
                             "install.servlet.dir property");
        }
    }

    public void verifyFileExists(boolean isDir, String file,
            String description, String option) throws FileNotFoundException {
        File f;

        if (file == null) {
            throw new FileNotFoundException("The user most provide the "
                    + "location of " + description
                    + ", but this is not specified.  " + "Use the " + option
                    + " to specify this file.");
        }

        m_out.print("- using " + description + "... ");

        f = new File(file);

        if (!f.exists()) {
            throw new FileNotFoundException(description
                    + " does not exist at \"" + file + "\".  Use the "
                    + option + " to specify another location.");
        }

        if (!isDir) {
            if (!f.isFile()) {
                throw new FileNotFoundException(description
                        + " not a file at \"" + file + "\".  Use the "
                        + option + " to specify another file.");
            }
        } else {
            if (!f.isDirectory()) {
                throw new FileNotFoundException(description
                        + " not a directory at \"" + file + "\".  Use the "
                        + option + " to specify " + "another directory.");
            }
        }

        m_out.println(f.getAbsolutePath());
    }

    public void checkWebappOldOpennmsDir() throws Exception {
        File f = new File(m_webappdir + File.separator + "opennms");

        m_out.print("- Checking for old opennms webapp directory in "
                + f.getAbsolutePath() + "... ");

        if (f.exists()) {
            throw new Exception("Old OpenNMS web application exists: "
                    + f.getAbsolutePath() + ".  You need to remove this "
                    + "before continuing.");
        }

        m_out.println("OK");
    }

    public void checkServerXmlOldOpennmsContext() throws Exception {
        String search_regexp = "(?ms).*<Context\\s+path=\"/opennms\".*";
        StringBuffer b = new StringBuffer();

        File f = new File(m_webappdir + File.separator + ".."
                + File.separator + "conf" + File.separator + "server.xml");

        m_out.print("- Checking for old opennms context in "
                + f.getAbsolutePath() + "... ");

        if (!f.exists()) {
            m_out.println("DID NOT CHECK (file does not exist)");
            return;
        }

        BufferedReader r = new BufferedReader(new FileReader(f));
        String line;

        while ((line = r.readLine()) != null) {
            b.append(line);
            b.append("\n");
        }
        r.close();

        if (b.toString().matches(search_regexp)) {
            throw new Exception(
                                "Old OpenNMS context found in "
                                        + f.getAbsolutePath()
                                        + ".  "
                                        + "You must remove this context from server.xml and re-run the "
                                        + "installer.");
        }

        m_out.println("OK");

        return;
    }

    public void installWebApp() throws Exception {
        m_out.println("- Install OpenNMS webapp... ");

        installLink(m_install_servletdir + File.separator + "META-INF"
                + File.separator + "context.xml", m_webappdir
                + File.separator + "opennms.xml", "web application context",
                    false);

        m_out.println("- Installing OpenNMS webapp... DONE");
    }

    public void installLink(String source, String destination,
            String description, boolean recursive) throws Exception {

        String[] cmd;
        ProcessExec e = new ProcessExec(m_out, m_out);

        if (new File(destination).exists()) {
            m_out.print("  - " + destination + " exists, removing... ");
            removeFile(destination, description, recursive);
            m_out.println("REMOVED");
        }

        m_out.print("  - creating link to " + destination + "... ");

        cmd = new String[4];
        cmd[0] = "ln";
        cmd[1] = "-sf";
        cmd[2] = source;
        cmd[3] = destination;

        if (e.exec(cmd) != 0) {
            throw new Exception("Non-zero exit value returned while "
                    + "linking " + description + ", " + source + " into "
                    + destination);
        }

        m_out.println("DONE");
    }

    public void updateTomcatConf() throws Exception {
        File f = new File(m_tomcat_conf);

        // XXX give the user the option to set the user to something else?
        // if so, should we chown the appropriate OpenNMS files to the
        // tomcat user?
        //
        // XXX should we have the option to automatically try to determine
        // the tomcat user and chown the OpenNMS files to that user?

        m_out.print("- setting tomcat4 user to 'root'... ");

        BufferedReader r = new BufferedReader(new FileReader(f));
        StringBuffer b = new StringBuffer();
        String line;

        while ((line = r.readLine()) != null) {
            if (line.startsWith("TOMCAT_USER=")) {
                b.append("TOMCAT_USER=\"root\"\n");
            } else {
                b.append(line);
                b.append("\n");
            }
        }
        r.close();

        f.renameTo(new File(m_tomcat_conf + ".before-opennms-"
                + System.currentTimeMillis()));

        f = new File(m_tomcat_conf);
        PrintWriter w = new PrintWriter(new FileOutputStream(f));

        w.print(b.toString());
        w.close();

        m_out.println("done");
    }

    public void removeFile(String destination, String description,
            boolean recursive) throws IOException, InterruptedException,
            Exception {
        String[] cmd;
        ProcessExec e = new ProcessExec(m_out, m_out);

        if (recursive) {
            cmd = new String[3];
            cmd[0] = "rm";
            cmd[1] = "-r";
            cmd[2] = destination;
        } else {
            cmd = new String[2];
            cmd[0] = "rm";
            cmd[1] = destination;
        }
        if (e.exec(cmd) != 0) {
            throw new Exception("Non-zero exit value returned while "
                    + "removing " + description + ", " + destination
                    + ", using \"" + StringUtils.arrayToDelimitedString(cmd, " ") + "\"");
        }

        if (new File(destination).exists()) {
            throw new Exception("Could not delete existing " + description
                    + ": " + destination);
        }
    }

    public void printHelp() {
        m_out.println("usage:");
        m_out.println("      $OPENNMS_HOME/bin/install -h");
        m_out.println("      $OPENNMS_HOME/bin/install "
                + "[-r] [-x] [-N] [-R] [-c] [-d] [-i] [-s] [-U]");
        m_out.println("                                [-y] [-X]");
        m_out.println("                                "
                + "[-u <PostgreSQL admin user>]");
        m_out.println("                                "
                + "[-p <PostgreSQL admin password>]");
        m_out.println("                                "
                + "[-D <PostgreSQL database URL>]");
        m_out.println("                                "
                + "[-T <tomcat4.conf>]");
        m_out.println("                                "
                + "[-w <tomcat context directory>");
        m_out.println("                                "
                + "[-C <constraint>]");
        m_out.println("");
        m_out.println(m_required_options);
        m_out.println("");
        m_out.println("   -h    this help");
        m_out.println("");
        m_out.println("   -d    perform database actions");
        m_out.println("   -i    insert data into the database");
        m_out.println("   -s    update iplike postgres function");
        m_out.println("   -U    upgrade database to unicode, if needed");
        m_out.println("   -y    install web application (see -w)");
        m_out.println("");
        m_out.println("   -u    username of the PostgreSQL "
                + "administrator (default: \"" + m_pg_user + "\")");
        m_out.println("   -p    password of the PostgreSQL "
                + "administrator (default: \"" + m_pg_pass + "\")");
        m_out.println("   -D    JDBC URL of the PostgreSQL "
                + "database (default: \"" + m_pg_url + "\")");
        m_out.println("   -c    drop and recreate tables that already "
                + "exist");
        m_out.println("");
        m_out.println("   -T    location of tomcat.conf");
        m_out.println("   -w    location of tomcat's context directory");
        m_out.println("         (usually under conf/Catalina/localhost)");
        m_out.println("");
        m_out.println("   -r    run as an RPM install (does nothing)");
        m_out.println("   -x    turn on debugging for database data "
                + "transformation");
        m_out.println("   -N    ignore NOT NULL constraint checks when "
                + "transforming data");
        m_out.println("         useful after a table is reverted by a "
                + "previous run of the installer");
        m_out.println("   -R    do not revert a table to the original if "
                + "an error occurs when");
        m_out.println("         transforming data -- only used for debugging");
        m_out.println("   -C    fix rows that violate the specified "
                + "constraint -- sets key column in");
        m_out.println("         affected rows to NULL by default");
        m_out.println("   -X    drop rows that violate constraint instead of marking key column in");
        m_out.println("         affected rows to NULL (used with \"-C\")");

        System.exit(0);
    }

    public static void main(String[] argv) throws Exception {
        new Installer().install(argv);
    }

    public String checkServerVersion() throws IOException {
        File catalinaHome = new File(m_webappdir).getParentFile();
        String readmeVersion = getTomcatVersion(new File(catalinaHome,
                                                         "README.txt"));
        String runningVersion = getTomcatVersion(new File(catalinaHome,
                                                          "RUNNING.txt"));

        if (readmeVersion == null && runningVersion == null) {
            return null;
        } else if (readmeVersion != null && runningVersion != null) {
            return readmeVersion; // XXX what should be done here?
        } else if (readmeVersion != null && runningVersion == null) {
            return readmeVersion;
        } else {
            return runningVersion;
        }
    }

    public String getTomcatVersion(File file) throws IOException {
        if (file == null || !file.exists()) {
            return null;
        }
        Pattern p = Pattern.compile("The Tomcat (\\S+) Servlet/JSP Container");
        BufferedReader in = new BufferedReader(new FileReader(file));
        for (int i = 0; i < 5; i++) {
            String line = in.readLine();
            if (line == null) { // EOF
                in.close();
                return null;
            }
            Matcher m = p.matcher(line);
            if (m.find()) {
                in.close();
                return m.group(1);
            }
        }

        in.close();
        return null;
    }

    public void pingLocalhost() throws IOException {
        String host = "127.0.0.1";

        IcmpSocket m_socket = null;

        try {
                m_socket = new IcmpSocket();
        } catch (UnsatisfiedLinkError e) {
                m_out.println("UnsatisfiedLinkError while creating an "
                              + "IcmpSocket.  Most likely failed to load "
                              + "libjicmp.so.  Try setting the property "
                              + "'opennms.library.jicmp' to point at the "
                              + "full path name of the libjicmp.so shared "
                              + "library "
                              + "(e.g. 'java -Dopennms.library.jicmp=/some/path/libjicmp.so ...')");
                throw e;
        } catch (NoClassDefFoundError e) {
                m_out.println("NoClassDefFoundError while creating an "
                              + "IcmpSocket.  Most likely failed to load "
                              + "libjicmp.so.");
                throw e;
        } catch (IOException e) {
                m_out.println("IOException while creating an "
                              + "IcmpSocket.");
                throw e;
        }

        java.net.InetAddress addr = null;
        try {
            addr = java.net.InetAddress.getByName(host);
        } catch (java.net.UnknownHostException e) {
            m_out.println("UnknownHostException when looking up "
                           + host + ".");
            throw e;

        }

        m_out.println("PING " + host + " (" + addr.getHostAddress()
                      + "): 56 data bytes");

        short m_icmpId = 2;

        IcmpSocket.Stuff s = new IcmpSocket.Stuff(m_socket, m_icmpId);
        Thread t = new Thread(s);
        t.start();

        int count = 3;
        for (long attempt = 0; attempt < count; attempt++) {
            // build a packet
            org.opennms.netmgt.ping.Packet pingPkt =
                new org.opennms.netmgt.ping.Packet(attempt);
            pingPkt.setIdentity(m_icmpId);
            pingPkt.computeChecksum();
        
            // convert it to a datagram to be sent
            byte[] buf = pingPkt.toBytes();
            DatagramPacket sendPkt =
                new DatagramPacket(buf, buf.length, addr, 0);
            buf = null;
            pingPkt = null;

            try {
                m_socket.send(sendPkt);
            } catch (IOException e) {
                m_out.println("IOException received when sending packet.");
                throw e;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // do nothing
            }
        }

    }

    public InstallerDb getInstallerDb() {
        return m_installerDb;
    }

    public void setOutputStream(PrintStream out) {
        m_out = out;
        m_installerDb.setOutputStream(m_out);
    }
}
