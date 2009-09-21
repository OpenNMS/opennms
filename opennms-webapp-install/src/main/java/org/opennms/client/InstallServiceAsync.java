package org.opennms.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface InstallServiceAsync {
	public void checkOwnershipFileExists(AsyncCallback<Boolean> callback);
	public void getOwnershipFilename(AsyncCallback<String> callback);
	public void resetOwnershipFilename(AsyncCallback<Void> callback);
	public void setAdminPassword(String password, AsyncCallback<Void> callback);
	public void connectToDatabase(AsyncCallback<Boolean> callback);
	public void setDatabaseConfig(String arguments, AsyncCallback<Void> callback);
	public void updateDatabase(AsyncCallback<Void> callback);
	public void checkIpLike(AsyncCallback<Boolean> callback);
}
