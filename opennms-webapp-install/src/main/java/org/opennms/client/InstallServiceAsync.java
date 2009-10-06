package org.opennms.client;

import java.util.*;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface InstallServiceAsync {
	public void checkOwnershipFileExists(AsyncCallback<Boolean> callback);
	public void getOwnershipFilename(AsyncCallback<String> callback);
	public void resetOwnershipFilename(AsyncCallback<Void> callback);
	public void setAdminPassword(String password, AsyncCallback<Void> callback);
	public void connectToDatabase(String dbName, String user, String password, String driver, String url, String binaryDirectory, AsyncCallback<Boolean> callback) throws IllegalStateException;
	// protected void setDatabaseConfig(String dbName, String user, String password, String driver, String url, String binaryDirectory, AsyncCallback<Void> callback);
	public void getDatabaseUpdateLogs(int offset, AsyncCallback<List<LoggingEvent>> callback);
	public void updateDatabase(AsyncCallback<Void> callback);
	public void checkIpLike(AsyncCallback<Boolean> callback);
}
