package org.opennms.client;

import java.util.*;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface InstallServiceAsync {
	public void checkOwnershipFileExists(AsyncCallback<Boolean> callback);
	public void getOwnershipFilename(AsyncCallback<String> callback);
	public void resetOwnershipFilename(AsyncCallback<Void> callback);
	public void isAdminPasswordSet(AsyncCallback<Boolean> callback);
	public void setAdminPassword(String password, AsyncCallback<Void> callback);
	public void connectToDatabase(String dbName, String user, String password, String driver, String adminUrl, String url, AsyncCallback<Void> callback) throws IllegalStateException;
	public void createDatabase(String dbName, String user, String password, String driver, String adminUrl, AsyncCallback<Void> callback) throws IllegalStateException;
	// protected void setDatabaseConfig(String dbName, String user, String password, String driver, String url, String binaryDirectory, AsyncCallback<Void> callback);
	public void getDatabaseUpdateLogs(int offset, AsyncCallback<List<LoggingEvent>> callback);
	public void clearDatabaseUpdateLogs(AsyncCallback<Void> callback);
	public void updateDatabase(AsyncCallback<Void> callback);
	public void isUpdateInProgress(AsyncCallback<Boolean> callback);
	public void didLastUpdateSucceed(AsyncCallback<Boolean> callback);
	public void checkIpLike(AsyncCallback<Boolean> callback);
}
