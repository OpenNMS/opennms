package org.opennms.client;

import java.util.*;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * <p>Interface that defines the server-side RPC functions that drive the
 * installation UI.</p>
 *
 * <p>TODO: Do we need additional functions?</p>
 * <ul>
 * <li>isDatabaseUpToDate()</li>
 * </ul>
 *
 * @author ranger
 * @version $Id: $
 */
@RemoteServiceRelativePath("install") // This path must match the value of the servlet mapping in web.xml
public interface InstallService extends RemoteService {
    /**
     * Check to see if the ownership file exists in the OpenNMS home directory.
     *
     * @return True if the file exists, false otherwise
     */
    public boolean checkOwnershipFileExists();

    /**
     * Fetch the expected name of the ownership file. This name will be randomly
     * generated each time the webapp is started up.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getOwnershipFilename();

    /**
     * Reset the name of the ownership file to a new value.
     */
    public void resetOwnershipFilename();

    /**
     * Check to see if the admin password has been set.
     *
     * @return True if the password is not null, false otherwise
     * @throws org.opennms.client.OwnershipNotConfirmedException if any.
     */
    public boolean isAdminPasswordSet() throws OwnershipNotConfirmedException;

    /**
     * Update the admin password to the specified value.
     *
     * @throws org.opennms.client.UserUpdateException if any.
     * @throws org.opennms.client.UserConfigFileException if any.
     * @param password a {@link java.lang.String} object.
     * @throws org.opennms.client.OwnershipNotConfirmedException if any.
     */
    public void setAdminPassword(String password) throws OwnershipNotConfirmedException, UserConfigFileException, UserUpdateException;

    /**
     * Fetch the current database settings from the <code>opennms-datasources.xml</code>
     * configuration file. This call is used to prepopulate the database settings form
     * with default or existing data.
     *
     * @throws org.opennms.client.DatabaseConfigFileException if any.
     * @return a {@link org.opennms.client.DatabaseConnectionSettings} object.
     * @throws java.lang.IllegalStateException if any.
     * @throws org.opennms.client.OwnershipNotConfirmedException if any.
     */
    public DatabaseConnectionSettings getDatabaseConnectionSettings() throws IllegalStateException, OwnershipNotConfirmedException, DatabaseConfigFileException;

    /**
     * Attempt to connect to the database and perform a lightweight database
     * test to ensure that our database connection parameters are successfully
     * connecting to a proper OpenNMS database. This method will throw exceptions
     * if the connection failed or the parameters cannot be stored.
     *
     * @throws org.opennms.client.DatabaseDriverException if any.
     * @throws org.opennms.client.DatabaseAccessException if any.
     * @throws org.opennms.client.DatabaseConfigFileException if any.
     * @throws org.opennms.client.IllegalDatabaseArgumentException if any.
     * @param driver a {@link java.lang.String} object.
     * @param dbName a {@link java.lang.String} object.
     * @param dbAdminUser a {@link java.lang.String} object.
     * @param dbAdminPassword a {@link java.lang.String} object.
     * @param dbAdminUrl a {@link java.lang.String} object.
     * @param dbNmsUser a {@link java.lang.String} object.
     * @param dbNmsPassword a {@link java.lang.String} object.
     * @param dbNmsUrl a {@link java.lang.String} object.
     * @throws java.lang.IllegalStateException if any.
     * @throws org.opennms.client.DatabaseDoesNotExistException if any.
     * @throws org.opennms.client.OwnershipNotConfirmedException if any.
     */
    public void connectToDatabase(String driver, String dbName, String dbAdminUser, String dbAdminPassword, String dbAdminUrl, String dbNmsUser, String dbNmsPassword, String dbNmsUrl) throws IllegalStateException, DatabaseDoesNotExistException, OwnershipNotConfirmedException, DatabaseDriverException, DatabaseAccessException, DatabaseConfigFileException, IllegalDatabaseArgumentException;

    /**
     * Attempt to connect to the database and perform a lightweight database
     * test to ensure that our database connection parameters are successfully
     * connecting to a proper OpenNMS database. This method will throw exceptions
     * if the connection failed or the parameters cannot be stored.
     *
     * @throws org.opennms.client.DatabaseCreationException if any.
     * @throws org.opennms.client.DatabaseUserCreationException if any.
     * @throws org.opennms.client.DatabaseAlreadyExistsException if any.
     * @throws org.opennms.client.DatabaseAccessException if any.
     * @throws org.opennms.client.DatabaseDriverException if any.
     * @throws org.opennms.client.IllegalDatabaseArgumentException if any.
     * @param driver a {@link java.lang.String} object.
     * @param dbName a {@link java.lang.String} object.
     * @param dbAdminUser a {@link java.lang.String} object.
     * @param dbAdminPassword a {@link java.lang.String} object.
     * @param dbAdminUrl a {@link java.lang.String} object.
     * @param dbNmsUser a {@link java.lang.String} object.
     * @param dbNmsPassword a {@link java.lang.String} object.
     * @throws org.opennms.client.OwnershipNotConfirmedException if any.
     */
    public void createDatabase(String driver, String dbName, String dbAdminUser, String dbAdminPassword, String dbAdminUrl, String dbNmsUser, String dbNmsPassword) throws OwnershipNotConfirmedException, DatabaseDriverException, DatabaseAccessException, DatabaseAlreadyExistsException, DatabaseUserCreationException, DatabaseCreationException, IllegalDatabaseArgumentException;

    // protected void setDatabaseConfig(String dbName, String user, String password, String driver, String url, String binaryDirectory);

    /**
     * Fetch all of the Log4J logs that have been generated by code running in the webapp JVM.
     *
     * @return List of serializable {@link LoggingEvent} instances
     * @param offset a int.
     * @throws org.opennms.client.OwnershipNotConfirmedException if any.
     */
    public List<LoggingEvent> getDatabaseUpdateLogs(int offset) throws OwnershipNotConfirmedException;

    /**
     * Fetch all of the Log4J log messages (as rendered strings) that have been generated by
     * code running in the webapp JVM.
     *
     * @return List of log message String instances
     * @throws org.opennms.client.OwnershipNotConfirmedException if any.
     */
    public List<String> getDatabaseUpdateLogsAsStrings() throws OwnershipNotConfirmedException;

    /**
     * Fetch the progress of the database installer subtasks.
     *
     * @return a {@link java.util.List} object.
     * @throws org.opennms.client.OwnershipNotConfirmedException if any.
     */
    public List<InstallerProgressItem> getDatabaseUpdateProgress() throws OwnershipNotConfirmedException;

    /**
     * Flush all of the accumulated log entries to start over with a blank list of log entries.
     *
     * @throws org.opennms.client.OwnershipNotConfirmedException if any.
     */
    public void clearDatabaseUpdateLogs() throws OwnershipNotConfirmedException;

    /**
     */
    /**
     * Spawn a thread to run the OpenNMS {@link org.opennms.install.Installer} class to update the database schema.
     * This method will spawn a new thread to perform the updates and
     * while the thread runs, the return value of {@link #isUpdateInProgress()}
     * will be <code>true</code>. Log messages that are generated will be made available to
     * the web UI by the {@link org.opennms.server.ListAppender} log4j appender.
     *
     * @throws org.opennms.client.OwnershipNotConfirmedException if any.
     */
    public void updateDatabase() throws OwnershipNotConfirmedException;

    /**
     * Check to see if the {@link Thread} spawned by a call to {@link #updateDatabase()} is
     * still running.
     *
     * @return <code>true</code> if the thread spawned by {@link #updateDatabase()} is still running,
     * <code>false</code> if it has completed.
     * @throws org.opennms.client.OwnershipNotConfirmedException if any.
     */
    public boolean isUpdateInProgress() throws OwnershipNotConfirmedException;

    /**
     * Check to see if the last {@link Thread} spawned by {@link #updateDatabase()}
     * completed without throwing an exception.
     *
     * @return <code>true</code> if the thread spawned by {@link #updateDatabase()} completed
     * without throwing an exception, <code>false</code> if an exception was thrown before it completed.
     * @throws org.opennms.client.OwnershipNotConfirmedException if any.
     */
    public boolean didLastUpdateSucceed() throws OwnershipNotConfirmedException;

    /**
     * Check to see if the <code>iplike</code> database procedure is working
     * properly on the currently configured database connection.
     *
     * @throws org.opennms.client.DatabaseConfigFileException if any.
     * @throws org.opennms.client.DatabaseDriverException if any.
     * @throws org.opennms.client.DatabaseAccessException if any.
     * @return a {@link org.opennms.client.IpLikeStatus} object.
     * @throws org.opennms.client.OwnershipNotConfirmedException if any.
     */
    public IpLikeStatus checkIpLike() throws OwnershipNotConfirmedException, DatabaseConfigFileException, DatabaseDriverException, DatabaseAccessException;
}
