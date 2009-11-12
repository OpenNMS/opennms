package org.opennms.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface InstallServiceAsync {
	public void checkOwnershipFileExists(AsyncCallback<Boolean> callback);
	public void getOwnershipFilename(AsyncCallback<String> callback);
	public void resetOwnershipFilename(AsyncCallback<Void> callback);
	public void isAdminPasswordSet(AsyncCallback<Boolean> callback);
	public void setAdminPassword(String password, AsyncCallback<Void> callback);
	public void getDatabaseConnectionSettings(AsyncCallback<DatabaseConnectionSettings> callback);
	public void connectToDatabase(String driver, String dbName, String dbAdminUser, String dbAdminPassword, String dbAdminUrl, String dbNmsUser, String dbNmsPassword, String dbNmsUrl, AsyncCallback<Void> callback);
	public void createDatabase(String driver, String dbName, String dbAdminUser, String dbAdminPassword, String dbAdminUrl, String dbNmsUser, String dbNmsPassword, AsyncCallback<Void> callback);
	// protected void setDatabaseConfig(String dbName, String user, String password, String driver, String url, String binaryDirectory, AsyncCallback<Void> callback);
	public void getDatabaseUpdateLogs(int offset, AsyncCallback<List<LoggingEvent>> callback);
	public void getDatabaseUpdateLogsAsStrings(AsyncCallback<List<String>> callback);
	public void getDatabaseUpdateProgress(AsyncCallback<List<InstallerProgressItem>> callback);
	public void clearDatabaseUpdateLogs(AsyncCallback<Void> callback);
	public void updateDatabase(AsyncCallback<Void> callback);
	public void isUpdateInProgress(AsyncCallback<Boolean> callback);
	public void didLastUpdateSucceed(AsyncCallback<Boolean> callback);
	public void checkIpLike(AsyncCallback<IpLikeStatus> callback);
}
