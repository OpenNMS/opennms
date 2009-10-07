package org.opennms.client;

import java.util.*;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

// This path must match the value of the servlet mapping in web.xml
@RemoteServiceRelativePath("install")
public interface InstallService extends RemoteService {
	public boolean checkOwnershipFileExists();
	public String getOwnershipFilename();
	public void resetOwnershipFilename();
	public boolean isAdminPasswordSet();
	public void setAdminPassword(String password);
	public boolean connectToDatabase(String dbName, String user, String password, String driver, String url, String binaryDirectory) throws IllegalStateException;
	// protected void setDatabaseConfig(String dbName, String user, String password, String driver, String url, String binaryDirectory);
	public List<LoggingEvent> getDatabaseUpdateLogs(int offset);
	public void updateDatabase();
	public boolean isUpdateInProgress();
	public boolean checkIpLike();
}
