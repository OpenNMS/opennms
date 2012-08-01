/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.vulnscand;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.regexp.RE;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.VulnscandConfigFactory;
import org.opennms.netmgt.model.OnmsSeverity;

/**
 * <p>
 * This class is a proxy for communications with the Nessus security scanner
 * daemon, nessusd. It has been designed to:
 * </p>
 * <ul>
 * <li>Initiate scans against a single target IP</li>
 * <li>Use prepackaged "levels" of security scanning:
 * <ul>
 * <li>Level 1: Portscanning</li>
 * <li>Level 2: Profiling</li>
 * <li>Level 3: Intrusions</li>
 * <li>Level 4: Destructive Intrusions</li>
 * </ul>
 * </li>
 * <li>Communicate with the daemon using username and and password, instead of
 * SSL key exchange</li>
 * </ul>
 * 
 * <p>
 * Other functions (arbitrary plugin selection, SSL support) may be added in the
 * future.
 * </p>
 * 
 */
class NessusScan implements Runnable {
    /*
     * Put the md5_caching directive in to prevent the server
     * from sending the entire plugin list at startup
     */
    private static final String NTP_VERSION_STRING = "< NTP/1.2 >< md5_caching plugins_cve_id plugins_version>";

    private static final String NTP_CLIENT_ENTITY = "CLIENT";

    private static final String NTP_SERVER_ENTITY = "SERVER";

    private static final String NTP_USERNAME_PROMPT = "User : ";

    private static final String NTP_PASSWORD_PROMPT = "Password : ";

    private static final String NTP_SEP = " <|> ";

    private static final int PORTSCAN_PLUGIN_ID = 0;

    /** Constant <code>SCAN_SUCCESS=0</code> */
    public static final int SCAN_SUCCESS = 0;

    /** Constant <code>SCAN_HOST_DOWN=1</code> */
    public static final int SCAN_HOST_DOWN = 1;

    /** Constant <code>SCAN_FATAL_ERROR=2</code> */
    public static final int SCAN_FATAL_ERROR = 2;

    /** Constant <code>SCAN_NON_FATAL_ERROR=4</code> */
    public static final int SCAN_NON_FATAL_ERROR = 4;

    /** Constant <code>SCAN_COMPLETE=8</code> */
    public static final int SCAN_COMPLETE = 8;

    /**
     * Select the next vulnerabilityID from the sequence
     */
    private static final String SELECT_NEXT_ID = "SELECT NEXTVAL('vulnNxtId')";

    /**
     * Get all unresolved vulnerabilities for a given ipaddr.
     */
    private static final String SELECT_ALL_VULNERABILITIES = "SELECT vulnerabilityid FROM vulnerabilities " + "WHERE ipaddr = ? AND resolvedtime IS NULL";

    /**
     * This query retrieves the name and summary of a plugin out of the database
     * so that we can construct the logmsg of the vulnerability with the fields.
     */
    private static final String SELECT_PLUGIN_INFO = "SELECT name, summary FROM vulnplugins " + "WHERE pluginid = ? AND pluginsubid = ?";

    /**
     * Insert a new vulnerability into the "vulnerabilities" table
     */
    private static final String INSERT_NEW_VULNERABILITY = "INSERT INTO vulnerabilities " + "(vulnerabilityid, nodeid, ipaddr, serviceid, creationtime, lastattempttime, lastscantime, " + "severity, pluginid, pluginsubid, logmsg, descr, port, protocol, cveentry) " + "VALUES (?,?,?,?, ?,?,?, ?, ?,?, ?,?, ?,?, ?)";

    /**
     * Find an open vulnerability ID in the database. The combination of ipaddr,
     * port, protocol, pluginID, and pluginSubID is used as the key for
     * vulnerability uniqueness.
     */
    private static final String SELECT_OPEN_VULNERABILITY = "SELECT vulnerabilityid FROM vulnerabilities " + "WHERE ipaddr = ? AND port = ? AND protocol = ? AND pluginid = ? AND pluginsubid = ? AND resolvedtime IS NULL";

    /**
     * Update the timestamps in an open vulnerability that was rescanned and
     * still exists.
     */
    private static final String VULNERABILITY_SCANNED = "UPDATE vulnerabilities SET lastattempttime = ?, lastscantime = ? " + "WHERE vulnerabilityid = ?";

    /**
     * Update the timestamps in an open vulnerability for which the rescan
     * failed.
     */
    private static final String VULNERABILITY_SCAN_ATTEMPTED = "UPDATE vulnerabilities SET lastattempttime = ? " + "WHERE vulnerabilityid = ?";

    /**
     * Resolve a given vulnerability by its unique ID number.
     */
    private static final String RESOLVE_VULNERABILITY = "UPDATE vulnerabilities SET lastattempttime = ?, resolvedtime = ? " + "WHERE vulnerabilityid = ?";

    /**
     * Nessus configuration that is used to perform the current scan.
     */
    private NessusScanConfiguration config;

    /**
     * Regex expression that is used to tokenize the messages from Nessus.
     */
    private RE ntpTokenizer = null;

    /**
     * List of the open vulnerabilities on an IP address
     */
    private Set<Integer> openVulnerabilities = new HashSet<Integer>();

    /**
     * Array that holds the plugin lists for each scanning level.
     */
    private String[] pluginLists = null;

    /**
     * Array that holds the safe-checks settings for each scanning level.
     */
    private boolean[] safeChecks = null;

    /**
     * Integer of the ordinal of the last plugin that was executed against the
     * target. This number is used to ensure that if the scan is terminated
     * prematurely because of an unreachable host in the list, the
     * vulnerabilities are not marked resolved.
     */
    private int lastPlugin;

    /**
     * Counter of the total number of plugins that will be executed against this
     * host.
     */
    private int totalPlugins;

    /**
     * Create a new scan that will scan the target specified in the
     * configuration and insert the results of the scan into the database.
     *
     * @param newConfig a {@link org.opennms.netmgt.vulnscand.NessusScanConfiguration} object.
     * @throws java.lang.IllegalArgumentException if any.
     */
    public NessusScan(NessusScanConfiguration newConfig) throws IllegalArgumentException {
        if (newConfig.isValid()) {
            config = newConfig;
        } else {
            throw new IllegalArgumentException("NessusScanConfiguration was invalid");
        }

        try {
            ntpTokenizer = new RE(" <\\|> ");
        } catch (org.apache.regexp.RESyntaxException ex) {
            ThreadCategory.getInstance(NessusScan.class).error("FATAL ERROR in regex in NessusScan.java. Correct this error and rebuild.", ex);
        }

        init();
    }

    private void init() {
        pluginLists = VulnscandConfigFactory.getInstance().getPluginLists();
        safeChecks = VulnscandConfigFactory.getInstance().getSafeChecks();

        lastPlugin = -1;
        totalPlugins = -1;
    }

    /**
     * <p>run</p>
     */
    public void run() {
        ThreadCategory log = ThreadCategory.getInstance(getClass());

        // Queue of the lines that are read from the Nessus socket
        FifoQueue<String> lines = null;
        // Flag that lets us know if we've found what we're looking for
        boolean found = false;
        // DB connection; is connected and disconnected as necessary
        Connection conn = null;

        /*
         * Grab the list of all current open vulnerabilities for the IP address.
         * We'll use this list to resolve vulnerabilities that are not
         * redetected.
         */
        final DBUtils d = new DBUtils(getClass());
        try {
            conn = DataSourceFactory.getInstance().getConnection();
            d.watch(conn);
        } catch (SQLException ex) {
            log.error("Could not open DB connection", ex);
            return;
        }
        try {
            PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_VULNERABILITIES);
            d.watch(stmt);

            stmt.setString(1, InetAddressUtils.str(config.targetAddress));
            ResultSet openVulnerabilitiesRS = stmt.executeQuery();
            d.watch(openVulnerabilitiesRS);

            while (openVulnerabilitiesRS.next()) {
                openVulnerabilities.add(new Integer(openVulnerabilitiesRS.getInt("vulnerabilityid")));
            }
        } catch (SQLException ex) {
            log.error("Error when querying database for open vulnerabilities.");
            log.error(ex.getLocalizedMessage(), ex);
            return;
        } finally {
            d.cleanUp();
        }

        /*
         * Perform a Nessus scan of the target IP address.  As each
         * vulnerability is found, a new entry is put into the database
	 * or the existing entry is updated.
         */
        Socket nessusSocket = null;
        try {
            nessusSocket = NessusConnectionFactory.getConnection(config.hostname, config.hostport);

            if (nessusSocket == null) {
                throw new IOException("Factory returned null connection");
            }

            InputStream in = nessusSocket.getInputStream();
            OutputStream out = nessusSocket.getOutputStream();

            log.debug("Attached streams to the Nessus socket.");

            // Login to the server
            out.write((NTP_VERSION_STRING + "\n").getBytes());

            log.debug("Sent NTP version string.");

            lines = readLines(in);

            // Strip off the protocol/username prompt
            found = false;
            while (!found) {
                while (lines.size() > 0) {
                    String line = (String) lines.remove();
                    log.debug("NTP string response: " + line);
                    if (line.indexOf(NTP_USERNAME_PROMPT) != -1) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    lines = readLines(in);
                }
            }

            // Login to the server
            out.write((config.username + "\n").getBytes());

            log.debug("Sent username string.");

            // Strip off the password prompt
            found = false;
            while (!found) {
                while (lines.size() > 0) {
                    String line = (String) lines.remove();
                    log.debug("Username response: " + line);
                    if (line.indexOf(NTP_PASSWORD_PROMPT) != -1) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    lines = readLines(in);
                }
            }

            // Login to the server
            out.write((config.password + "\n").getBytes());

            log.debug("Sent password string.");

            // Strip off the rules list
            found = false;
            while (!found) {
                while (lines.size() > 0) {
                    String line = (String) lines.remove();

                    // Do any necessary parsing
                    log.debug("Password response: " + line);

                    if (line.indexOf((NTP_SEP + NTP_SERVER_ENTITY).trim()) != -1) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    lines = readLines(in);
                }
            }

            // Strip off the preferences list
            found = false;
            while (!found) {
                while (lines.size() > 0) {
                    String line = (String) lines.remove();

                    // Do any necessary parsing
                    log.debug("Preferences: " + line);

                    if (line.indexOf((NTP_SEP + NTP_SERVER_ENTITY).trim()) != -1) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    lines = readLines(in);
                }
            }

            // Strip off the rules list
            found = false;
            while (!found) {
                while (lines.size() > 0) {
                    String line = (String) lines.remove();

                    // Do any necessary parsing
                    log.debug("Rules: " + line);

                    if (line.indexOf((NTP_SEP + NTP_SERVER_ENTITY).trim()) != -1) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    lines = readLines(in);
		}
            }

            /*
             * Write the preferences list for the scan
             * (which includes the list of plugins to execute
             * against the target).
             */
            out.write(buildPreferencesString().getBytes());

            log.debug("Sent preferences string.");

            // Strip off the PREFERENCES_ERRORS
            found = false;
            while (!found) {
                while (lines.size() > 0) {
                    String line = (String) lines.remove();

                    // Do any necessary parsing
                    log.debug("Preferences response: " + line);

                    if (line.indexOf((NTP_SEP + NTP_SERVER_ENTITY).trim()) != -1) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    lines = readLines(in);
            }

            /*
             * I'm using the NEW_ATTACK directive, since I don't
             * care about command strings getting too long (which
             * you would use the LONG_ATTACK directive for).
             */
            
            //out.write((NTP_CLIENT_ENTITY + NTP_SEP + "NEW_ATTACK" + NTP_SEP + config.targetAddress.toString().replaceAll("/", "") + NTP_SEP + NTP_CLIENT_ENTITY + "\n").getBytes());
//            out.write((NTP_CLIENT_ENTITY + NTP_SEP + "NEW_ATTACK" + NTP_SEP + config.targetAddress.toString().replaceAll("/", "") + NTP_SEP + NTP_CLIENT_ENTITY + "\n").getBytes());
            out.write((NTP_CLIENT_ENTITY + NTP_SEP + "NEW_ATTACK" + NTP_SEP + config.targetAddress.getCanonicalHostName() + NTP_SEP + NTP_CLIENT_ENTITY + "\n").getBytes());

            log.debug("Sent NEW_ATTACK directive against target: " + config.targetAddress.getCanonicalHostName());

            // Read the response to the NEW_ATTACK
            int returnCode = SCAN_SUCCESS;

            while ((returnCode == SCAN_SUCCESS) || (returnCode == SCAN_NON_FATAL_ERROR)) {
                while (lines.size() > 0) {
                    String line = (String) lines.remove();

                    log.debug("Nessus attack response: " + line.replace('\n', ' '));

                    // Grep out any inappropriate messages
                    if ((line.indexOf("the server killed it") == -1)) {
                        /*
                         * This processing will update existing vulnerabilities
                         * in the database and add new vulnerability entries
                         * as the vulnerabilities are detected.
                         */
                        returnCode = processScanMessage(line);
                    } else {
                        log.error("Discarded inappropriate Nessus message: " + line);
                    }
                }

                // If the last read was successful, get more lines
                if ((returnCode == SCAN_SUCCESS) || (returnCode == SCAN_NON_FATAL_ERROR)) {
                    lines = readLines(in);
                }
            }

            out.write(buildStopWholeTestString().getBytes());

            /*
             * If there were open vulnerabilities that were not reconfirmed
             * during this scanning cycle, then mark them as resolved.
             */
            if (openVulnerabilities.size() > 0) {
                try {
                    conn = DataSourceFactory.getInstance().getConnection();
                    d.watch(conn);
                } catch (SQLException ex) {
                    log.error("Could not open DB connection", ex);
                    return;
                }
                try {
                    PreparedStatement stmt = conn.prepareStatement(RESOLVE_VULNERABILITY);
                    d.watch(stmt);

                    Timestamp currentTime = new Timestamp(new java.util.Date().getTime());

                    Iterator<Integer> vuln = openVulnerabilities.iterator();
                    while (vuln.hasNext()) {
                        stmt.setTimestamp(1, currentTime);

                        /*
                         * If the scan ended because of a successful completion
                         * and all plugins were executed (indicating that the
                         * host WAS accessible), resolve the bug.
                         */
                        if ((returnCode == SCAN_COMPLETE) && (lastPlugin == totalPlugins)) {
                            stmt.setTimestamp(2, currentTime);
                        }
                        // Otherwise, just leave the resolved field NULL
                        else {
                            stmt.setNull(2, Types.TIMESTAMP);
                        }

                        stmt.setInt(3, ((Integer) vuln.next()).intValue());

                        stmt.executeUpdate();
                    }
                } catch (SQLException ex) {
                    log.error("Error when querying database for open vulnerabilities.");
                    log.error(ex.getLocalizedMessage(), ex);
                    return;
                } finally {
                    d.cleanUp();
                }
            }

            log.debug("Sent STOP_WHOLE_TEST directive against target " + config.targetAddress.toString());
        } catch (FifoQueueException ex) {
            log.warn(ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            log.warn(ex.getMessage(), ex);
        } catch (IOException ex) {
            log.warn(ex.getMessage(), ex);
        } finally {
            log.info("Releasing Nessus socket connection");
            if (nessusSocket != null) {
                NessusConnectionFactory.releaseConnection(nessusSocket);
	    }
        }

        // Update the scheduler flags for this configuration
        config.setScheduled(false);
        config.setLastScanned(new java.util.Date());
    }

    private String buildStopWholeTestString() {
        return (NTP_CLIENT_ENTITY + NTP_SEP + "STOP_WHOLE_TEST" + NTP_SEP + NTP_CLIENT_ENTITY + "\n");
    }

    @SuppressWarnings("unused")
	private String buildStopScanString() {
        return (NTP_CLIENT_ENTITY + NTP_SEP + "STOP_ATTACK" + config.targetAddress.toString() + NTP_SEP + NTP_CLIENT_ENTITY + "\n");
    }

    /**
     * Build the preferences string with the appropriate plugins and safe_checks
     * settings from the config file.
     */
    private String buildPreferencesString() {
        String retval = NTP_CLIENT_ENTITY + NTP_SEP + "PREFERENCES" + NTP_SEP + "\n" + "plugin_set" + NTP_SEP + pluginLists[config.scanLevel] + "\n" + "safe_checks" + NTP_SEP;

        if (safeChecks[config.scanLevel])
            retval += "yes";
        else
            retval += "no";

        retval += "\nmax_hosts" + NTP_SEP + "1\n" + "ntp_short_status" + NTP_SEP + "yes\n" +  "reverse_lookup" + NTP_SEP + "yes\n" + NTP_SEP + NTP_CLIENT_ENTITY;

        return retval;
    }

    /**
     * Process a scan message.
     * <p>
     * This function is designed to parse any messages that come from Nessus
     * during a scan session (eg. after the NEW_ATTACK directive has been sent)
     * </p>
     * 
     * <p>
     * The following types of events are handled:
     * <ul>
     * <li>Abbreviated STATUS messages</li>
     * <li>PORT</li>
     * <li>INFO</li>
     * <li>HOLE</li>
     * <li>BYE (as in BYE BYE)</li>
     * </ul>
     * </p>
     * 
     */
    private int processScanMessage(String message) {
        ThreadCategory log = ThreadCategory.getInstance(getClass());

        int vulnerabilityId = -1;

        // DB connection; is connected and disconnected as necessary
        Connection conn = null;

        // Normal NTP messages
        if (message.startsWith((NTP_SERVER_ENTITY + NTP_SEP).trim())) {
            int i = 0;

            message = message.substring((NTP_SERVER_ENTITY + NTP_SEP).length()).trim();

            String tokens[] = ntpTokenizer.split(message);

            String next = tokens[i++];

            /*
             * Indicates information about the target system or
             * that a security hole has been located.
             */
            if (next.equals("INFO") || next.equals("HOLE")) {
                NessusParser parser = NessusParser.getInstance();

                PortValues portvals = null;
                DescrValues descrvals = null;
                int pluginId = -1, pluginSubId = -1;
                String pluginLogmsg = "";

                String hostname = tokens[i++];

                String portString = tokens[i++];
                try {
                    // Parse the service, port, and protocol of the hole
                    portvals = parser.parsePort(portString);
                } catch (IllegalArgumentException ex) {
                    log.error("Could not parse the port and protocol information out of this string: " + portString);

                    portvals = NessusParser.getDefaultPortValues();
                }

                String descrString = tokens[i++];
                try {
                    // Parse the descr of the event
                    descrvals = parser.parseDescr(descrString);
                } catch (IllegalArgumentException ex) {
                    log.error("Could not parse the severity, descr, and/or CVE entry information out of this string: \n" + descrString);

                    descrvals = NessusParser.getDefaultDescrValues();
                }

                String pluginIdString = tokens[i++];
                try {
                    pluginId = Integer.parseInt(pluginIdString);
                } catch (NumberFormatException ex) {
                    log.error("Could not parse the plugin ID out of the string: " + pluginIdString, ex);
                }

                /*
                 * Change this, once we get a way to break the
                 * plugins down into separate vulnerabilities.
                 */
                pluginSubId = 0;

                final DBUtils d = new DBUtils(getClass());
                try {
                    conn = DataSourceFactory.getInstance().getConnection();
                    d.watch(conn);
                } catch (SQLException ex) {
                    log.error("Could not open DB connection", ex);
                    return SCAN_FATAL_ERROR;
                }
                try {
                    PreparedStatement stmt = conn.prepareStatement(SELECT_OPEN_VULNERABILITY);
                    d.watch(stmt);

                    // ipaddr
                    stmt.setString(1, InetAddressUtils.str(config.targetAddress));

                    // port
                    if (portvals.port > 0) {
                        stmt.setInt(2, portvals.port);
                    } else {
                        stmt.setNull(2, Types.INTEGER);
                    }

                    // protocol
                    if (portvals.protocol != null) {
                        stmt.setString(3, portvals.protocol);
                    } else {
                        stmt.setNull(2, Types.VARCHAR);
                    }

                    // pluginid and pluginsubid
                    stmt.setInt(4, pluginId);
                    stmt.setInt(5, pluginSubId);

                    ResultSet openVuln = stmt.executeQuery();
                    d.watch(openVuln);

                    // Update the timestamps on the existing events
                    if (openVuln.next()) {
                        stmt = conn.prepareStatement(VULNERABILITY_SCANNED);

                        Timestamp currentTime = new Timestamp(new java.util.Date().getTime());
                        stmt.setTimestamp(1, currentTime);
                        stmt.setTimestamp(2, currentTime);
                        stmt.setInt(3, openVuln.getInt("vulnerabilityid"));

                        int rowCount = stmt.executeUpdate();
                        if (rowCount != 1) {
                            log.error("UNEXPECTED CONDITION: " + rowCount + " row(s) updated during last scan successful UPDATE call");
                        } else {
                            openVulnerabilities.remove(new Integer(openVuln.getInt("vulnerabilityid")));
                        }

                        if (openVuln.next()) {
                            log.error("UNEXPECTED CONDITION: There are multiple rows that match this vulnerability, ignoring subsequent rows.");
                        }
                    }
                    // Insert a new vulnerability row into the database
                    else {
                        stmt = conn.prepareStatement(SELECT_NEXT_ID);
                        d.watch(stmt);
                        ResultSet idRS = stmt.executeQuery();
                        d.watch(idRS);
                        idRS.next();
                        int vulnId = idRS.getInt(1);
                        idRS.close();
                        idRS = null;

                        stmt = conn.prepareStatement(INSERT_NEW_VULNERABILITY);
                        d.watch(stmt);

                        stmt.setInt(1, vulnId);

                        // Match the interface to a node in the database
                        int nodeId = VulnscandConfigFactory.getInterfaceDbNodeId(conn, config.targetAddress);
                        if (nodeId > 0) {
                            stmt.setInt(2, nodeId);
                        } else {
                            stmt.setNull(2, Types.INTEGER);
			}

                        stmt.setString(3, InetAddressUtils.str(config.targetAddress));
                        // ADD SERVICE CORRELATION
                        // Punt this for now also... not necessary
                        // stmt.setInt(4, serviceId);
                        stmt.setNull(4, Types.INTEGER);

                        Timestamp currentTime = new Timestamp(new java.util.Date().getTime());
                        stmt.setTimestamp(5, currentTime);
                        stmt.setTimestamp(6, currentTime);
                        stmt.setTimestamp(7, currentTime);

                        stmt.setInt(8, descrvals.severity);

                        stmt.setInt(9, pluginId);
                        stmt.setInt(10, pluginSubId);

                        PreparedStatement pluginStmt = conn.prepareStatement(SELECT_PLUGIN_INFO);
                        d.watch(pluginStmt);
                        pluginStmt.setInt(1, pluginId);
                        pluginStmt.setInt(2, pluginSubId);
                        ResultSet plugRS = pluginStmt.executeQuery();
                        d.watch(plugRS);
                        if (plugRS.next()) {
                            if (plugRS.getString("name") != null && plugRS.getString("name").length() > 0) {
                                pluginLogmsg = plugRS.getString("name");
                            }
                            if (plugRS.getString("summary") != null && plugRS.getString("summary").length() > 0) {
                                if (!pluginLogmsg.equals("")) {
                                    pluginLogmsg += ": ";
                                }
                                pluginLogmsg += plugRS.getString("summary");
                            }
                        }

                        if (pluginLogmsg.equals("")) {
            			    /*
            			     * XXX Add a method that will query the Nessus
            			     * XXX server for information directly if it
            			     * XXX cannot be located in the database.
            			     */
                            // Punt this for now; we will pre-populate the DB
                            if (portvals.port >= 0) {
                                pluginLogmsg = "A vulnerability was detected on port " + portvals.port + ". See the description for more information.";
                            } else {
                                pluginLogmsg = "A vulnerability was detected. See the description for " + "more information.";
                            }
                        }

                        stmt.setString(11, pluginLogmsg);

                        stmt.setString(12, descrvals.descr);

                        if (portvals.port >= 0) {
                            stmt.setInt(13, portvals.port);
                        } else {
                            stmt.setNull(13, Types.INTEGER);
                        }

                        stmt.setString(14, portvals.protocol);

                        if (descrvals.cveEntry != null) {
                            stmt.setString(15, descrvals.cveEntry);
                        } else {
                            stmt.setNull(15, Types.VARCHAR);
                        }

                        if (stmt.executeUpdate() < 1) {
                            log.error("UNEXPECTED CONDITION: No rows inserted during last INSERT call.");
                        }
                    }
                } catch (SQLException ex) {
                    log.error("Error when querying database after " + next + " was found");
                    log.error(ex.getLocalizedMessage(), ex);
                    return SCAN_FATAL_ERROR;
                } finally {
                    d.cleanUp();
                }
                return SCAN_SUCCESS;
            }
            // Indicates that a port/protocol is open
            else if (next.equals("PORT")) {
                NessusParser parser = NessusParser.getInstance();

                PortValues portvals = null;
                int pluginId = -1, pluginSubId = -1;

                String hostname = tokens[i++];

                String portString = tokens[i++];
                try {
                    // Parse the service, port, and protocol of the hole
                    portvals = parser.parsePort(portString);
                } catch (IllegalArgumentException ex) {
                    log.error("Could not parse the port and protocol information out of this string: " + portString);

                    portvals = NessusParser.getDefaultPortValues();
                }

                if (portvals.port < 0) {
                    log.error("Port could not be determined from Nessus PORT message (" + portvals.port + "), dropping the message.");
                    return SCAN_NON_FATAL_ERROR;
                }

                final DBUtils d = new DBUtils(getClass());
                try {
                    conn = DataSourceFactory.getInstance().getConnection();
                    d.watch(conn);
                } catch (SQLException ex) {
                    log.error("Could not open DB connection", ex);
                    return SCAN_FATAL_ERROR;
                }
                try {
                    PreparedStatement stmt = conn.prepareStatement(SELECT_OPEN_VULNERABILITY);
                    d.watch(stmt);

                    // ipaddr
                    stmt.setString(1, InetAddressUtils.str(config.targetAddress));

                    // port
                    if (portvals.port > 0) {
                        stmt.setInt(2, portvals.port);
                    } else {
                        stmt.setNull(2, Types.INTEGER);
                    }

                    // protocol
                    if (portvals.protocol != null) {
                        stmt.setString(3, portvals.protocol);
                    } else {
                        stmt.setNull(2, Types.VARCHAR);
		    }

                    // pluginid and pluginsubid
                    stmt.setInt(4, PORTSCAN_PLUGIN_ID);
                    stmt.setInt(5, 0);

                    ResultSet openVuln = stmt.executeQuery();
                    d.watch(openVuln);

                    // Update the timestamps on the existing events
                    if (openVuln.next()) {
                        stmt = conn.prepareStatement(VULNERABILITY_SCANNED);
                        d.watch(stmt);

                        Timestamp currentTime = new Timestamp(new java.util.Date().getTime());
                        stmt.setTimestamp(1, currentTime);
                        stmt.setTimestamp(2, currentTime);
                        stmt.setInt(3, openVuln.getInt("vulnerabilityid"));

                        int rowCount = stmt.executeUpdate();
                        if (rowCount != 1) {
                            log.error("UNEXPECTED CONDITION: " + rowCount + " row(s) updated during last scan successful UPDATE call");
                        } else {
                            openVulnerabilities.remove(new Integer(openVuln.getInt("vulnerabilityid")));
                        }

                        if (openVuln.next()) {
                            log.error("UNEXPECTED CONDITION: There are multiple rows that match this vulnerability, ignoring subsequent rows.");
                        }
                    }
                    // Insert a new vulnerability row into the database
                    else {
                        stmt = conn.prepareStatement(SELECT_NEXT_ID);
                        d.watch(stmt);
                        ResultSet idRS = stmt.executeQuery();
                        d.watch(idRS);
                        idRS.next();
                        int vulnId = idRS.getInt(1);

                        stmt = conn.prepareStatement(INSERT_NEW_VULNERABILITY);
                        d.watch(stmt);

                        stmt.setInt(1, vulnId);

                        // Match the interface to a node in the database
                        int nodeId = VulnscandConfigFactory.getInterfaceDbNodeId(conn, config.targetAddress);
                        if (nodeId > 0) {
                            stmt.setInt(2, nodeId);
                        } else {
                            stmt.setNull(2, Types.INTEGER);
                        }

                        stmt.setString(3, InetAddressUtils.str(config.targetAddress));
                        // ADD SERVICE CORRELATION
                        // Punt this for now also... not necessary
                        // stmt.setInt(4, serviceId);
                        stmt.setNull(4, Types.INTEGER);

                        Timestamp currentTime = new Timestamp(new java.util.Date().getTime());
                        stmt.setTimestamp(5, currentTime);
                        stmt.setTimestamp(6, currentTime);
                        stmt.setTimestamp(7, currentTime);

                        // Use Normal severity for open ports
                        stmt.setInt(8, OnmsSeverity.NORMAL.getId());

                        stmt.setInt(9, PORTSCAN_PLUGIN_ID);
                        stmt.setInt(10, 0);

                        stmt.setString(11, "Port " + portvals.port + " is open on this host.");
                        stmt.setString(12, "Port " + portvals.port + " is open on this host.");

                        if (portvals.port >= 0) {
                            stmt.setInt(13, portvals.port);
                        } else {
                            stmt.setNull(13, Types.INTEGER);
                        }

                        // Protocol
                        stmt.setString(14, portvals.protocol);

                        // CVE entry
                        stmt.setNull(15, Types.VARCHAR);

                        if (stmt.executeUpdate() < 1) {
                            log.error("UNEXPECTED CONDITION: No rows inserted during last INSERT call.");
                        }
                    }
                } catch (SQLException ex) {
                    log.error("Error when querying database after " + next + " was found");
                    log.error(ex.getLocalizedMessage(), ex);
                    return SCAN_FATAL_ERROR;
                } finally {
                    d.cleanUp();
                }
                return SCAN_SUCCESS;
            } else if (next.equals("STATUS")) {
                // Shouldn't get any of these
                log.error("Weird... a non-abbreviated STATUS message. Check your code.");
                return SCAN_NON_FATAL_ERROR;
            } else if (next.equals("BYE")) {
                log.debug("BYE message received, ending scan");

                if (lastPlugin == totalPlugins) {
                    // If the scan completed running each plugin
                    return SCAN_COMPLETE;
		} else {
                    // Otherwise, do not resolve undetected plugins
                    return SCAN_FATAL_ERROR;
		}
            } else {
                log.warn("Unhandled message type from Nessus: " + next + "\n" + message);
                return SCAN_NON_FATAL_ERROR;
            }
        } else if (message.startsWith("s:")) {
            // Abbreviated status messages
            message = message.substring("s:".length()).trim();

            StringTokenizer parts = new StringTokenizer(message, ":");
            String type, hostname;
            int last, total;

            try {
                String next = parts.nextToken();

                if (next.equals("p")) {
                    type = "portscan";

                    // Ignore the parameters for portscans,
                    // always report SCAN_SUCCESS

                    return SCAN_SUCCESS;
                } else if (next.equals("a")) {
                    type = "attack";

                    hostname = parts.nextToken();
                    last = Integer.parseInt(parts.nextToken());
                    total = Integer.parseInt(parts.nextToken());

                    if (lastPlugin >= 0) {
                        /*
                         * If the plugin increment magically
                         * goes down because Nessus is
                         * starting another unwanted scan,
                         * report the scan complete so it
                         * will terminate the connection.
                         */
                        if (last < lastPlugin) {
                            log.warn("UNEXPECTED CONDITION: The completed plugin counter decreased. Reporting the current scan complete.");
                            return SCAN_COMPLETE;
                        }
                    }
                    lastPlugin = last;
                    log.debug("Last plugin: " + lastPlugin);

                    // Set the plugin total
                    if (totalPlugins <= 0) {
                        totalPlugins = total;
                        log.debug("Plugin total: " + totalPlugins);
                    }

                    return SCAN_SUCCESS;
                } else {
                    log.error("UNEXPECTED CONDITION: Invalid abbreviated status message from Nessus, discarding...  \n\t" + message);
                    return SCAN_NON_FATAL_ERROR;
                }
            } catch (NoSuchElementException ex) {
                log.error("UNEXPECTED CONDITION: Invalid abbreviated status message from Nessus, discarding...  \n\t" + message);
                return SCAN_FATAL_ERROR;
            } catch (NumberFormatException ex) {
                log.error("UNEXPECTED CONDITION: Could not parse integers out of this Nessus status message: " + message);
                return SCAN_FATAL_ERROR;
            }
        } else {
            log.warn("UNEXPECTED CONDITION: Unhandled message from Nessus: " + message);
            return SCAN_NON_FATAL_ERROR;
        }
    }

    /**
     * <p>readLines</p>
     *
     * @param in a {@link java.io.InputStream} object.
     * @return a {@link org.opennms.core.queue.FifoQueue} object.
     */
    public FifoQueue<String> readLines(InputStream in) {
        ThreadCategory log = ThreadCategory.getInstance(getClass());
        String EOL = "\n";

        String alreadyRecdData = null;

        FifoQueue<String> retval = new FifoQueueImpl<String>();

        ByteArrayOutputStream xmlStr = new ByteArrayOutputStream();
        int bytesInThisRead = 0;

        // loop until we've read it all or we have to shutdown
        while (true) {
            // read data off the socket's input stream
            try {
                byte[] message = new byte[1024];

                bytesInThisRead = in.read(message);
                if (log.isDebugEnabled()) {
                    log.debug("bytesInThisRead: " + bytesInThisRead);
		}

                /*
                 * Check the result code. A negative value
                 * means that the end of file has been reached
                 * Otherwise the value must be greater than zero
                 * according to the Java API documentation
                 */
                if (bytesInThisRead < 0) {
                    break;
		}

                /*
                 * Check if current chunk of data has end of data
                 * care should be exercised since the buffer may contain
                 * more than one log message.
                 */
                String newData = new String(message, 0, bytesInThisRead);
                String tempStr;
                if (alreadyRecdData != null) {
                    tempStr = alreadyRecdData + newData;
                } else {
                    tempStr = newData;
		}

                int index = -1;
                while ((index = tempStr.indexOf(EOL)) != -1) {
                    int tlen = index + EOL.length();
                    if (tlen > tempStr.length()) {
                        tlen = tempStr.length();
                    }

                    byte[] tempb = tempStr.substring(0, tlen).getBytes();
                    xmlStr.write(tempb, 0, tlen);

                    // Create a new text message
                    retval.add(new String(xmlStr.toByteArray(), 0, xmlStr.size()));

                    xmlStr.reset();
                    alreadyRecdData = null;

                    if (tlen != tempStr.length()) {
                        tempStr = tempStr.substring(tlen);
                    } else if (tlen == tempStr.length()) {
                        tempStr = "";
                    }
                }

                if (tempStr.length() != 0) {
                    alreadyRecdData = tempStr;
                }

                if (bytesInThisRead < 1024) {
                    /*
                     * Return any remaining data as the last line
                     * in the queue.
		     */
                    if ((alreadyRecdData != null) && (alreadyRecdData.length() != 0)) {
                        retval.add(alreadyRecdData);
                    }
                    break;
                }
            } catch (FifoQueueException ex) {
                log.warn(ex.getMessage());
            } catch (InterruptedException ex) {
                log.warn(ex.getMessage());
            } catch (IOException e) {
                log.warn(e.getMessage());
            }
        }
        return retval;
    }
}
