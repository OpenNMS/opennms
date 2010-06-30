package org.opennms.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * GWT asynchronous RPC facade for an {@link InstallService} GWT service.
 *
 * @author ranger
 * @version $Id: $
 */
public interface InstallServiceAsync {
	/**
	 * <p>checkOwnershipFileExists</p>
	 *
	 * @param callback a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
	 */
	public void checkOwnershipFileExists(AsyncCallback<Boolean> callback);
	/**
	 * <p>getOwnershipFilename</p>
	 *
	 * @param callback a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
	 */
	public void getOwnershipFilename(AsyncCallback<String> callback);
	/**
	 * <p>resetOwnershipFilename</p>
	 *
	 * @param callback a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
	 */
	public void resetOwnershipFilename(AsyncCallback<Void> callback);
	/**
	 * <p>isAdminPasswordSet</p>
	 *
	 * @param callback a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
	 */
	public void isAdminPasswordSet(AsyncCallback<Boolean> callback);
	/**
	 * <p>setAdminPassword</p>
	 *
	 * @param password a {@link java.lang.String} object.
	 * @param callback a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
	 */
	public void setAdminPassword(String password, AsyncCallback<Void> callback);
	/**
	 * <p>getDatabaseConnectionSettings</p>
	 *
	 * @param callback a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
	 */
	public void getDatabaseConnectionSettings(AsyncCallback<DatabaseConnectionSettings> callback);
	/**
	 * <p>connectToDatabase</p>
	 *
	 * @param driver a {@link java.lang.String} object.
	 * @param dbName a {@link java.lang.String} object.
	 * @param dbAdminUser a {@link java.lang.String} object.
	 * @param dbAdminPassword a {@link java.lang.String} object.
	 * @param dbAdminUrl a {@link java.lang.String} object.
	 * @param dbNmsUser a {@link java.lang.String} object.
	 * @param dbNmsPassword a {@link java.lang.String} object.
	 * @param dbNmsUrl a {@link java.lang.String} object.
	 * @param callback a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
	 */
	public void connectToDatabase(String driver, String dbName, String dbAdminUser, String dbAdminPassword, String dbAdminUrl, String dbNmsUser, String dbNmsPassword, String dbNmsUrl, AsyncCallback<Void> callback);
	/**
	 * <p>createDatabase</p>
	 *
	 * @param driver a {@link java.lang.String} object.
	 * @param dbName a {@link java.lang.String} object.
	 * @param dbAdminUser a {@link java.lang.String} object.
	 * @param dbAdminPassword a {@link java.lang.String} object.
	 * @param dbAdminUrl a {@link java.lang.String} object.
	 * @param dbNmsUser a {@link java.lang.String} object.
	 * @param dbNmsPassword a {@link java.lang.String} object.
	 * @param callback a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
	 */
	public void createDatabase(String driver, String dbName, String dbAdminUser, String dbAdminPassword, String dbAdminUrl, String dbNmsUser, String dbNmsPassword, AsyncCallback<Void> callback);
	// protected void setDatabaseConfig(String dbName, String user, String password, String driver, String url, String binaryDirectory, AsyncCallback<Void> callback);
	/**
	 * <p>getDatabaseUpdateLogs</p>
	 *
	 * @param offset a int.
	 * @param callback a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
	 */
	public void getDatabaseUpdateLogs(int offset, AsyncCallback<List<LoggingEvent>> callback);
	/**
	 * <p>getDatabaseUpdateLogsAsStrings</p>
	 *
	 * @param callback a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
	 */
	public void getDatabaseUpdateLogsAsStrings(AsyncCallback<List<String>> callback);
	/**
	 * <p>getDatabaseUpdateProgress</p>
	 *
	 * @param callback a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
	 */
	public void getDatabaseUpdateProgress(AsyncCallback<List<InstallerProgressItem>> callback);
	/**
	 * <p>clearDatabaseUpdateLogs</p>
	 *
	 * @param callback a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
	 */
	public void clearDatabaseUpdateLogs(AsyncCallback<Void> callback);
	/**
	 * <p>updateDatabase</p>
	 *
	 * @param callback a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
	 */
	public void updateDatabase(AsyncCallback<Void> callback);
	/**
	 * <p>isUpdateInProgress</p>
	 *
	 * @param callback a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
	 */
	public void isUpdateInProgress(AsyncCallback<Boolean> callback);
	/**
	 * <p>didLastUpdateSucceed</p>
	 *
	 * @param callback a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
	 */
	public void didLastUpdateSucceed(AsyncCallback<Boolean> callback);
	/**
	 * <p>checkIpLike</p>
	 *
	 * @param callback a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
	 */
	public void checkIpLike(AsyncCallback<IpLikeStatus> callback);
}
