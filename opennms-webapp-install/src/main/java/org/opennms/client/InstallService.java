package org.opennms.client;

import java.util.*;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("install")
public interface InstallService extends RemoteService {
	public boolean checkOwnershipFileExists();
	public String getOwnershipFilename();
	public void resetOwnershipFilename();
	public void setAdminPassword(String password);
	public boolean connectToDatabase();
	public void setDatabaseConfig(String arguments);
	public List<LoggingEvent> getDatabaseUpdateLogs(int offset);
	public void updateDatabase();
	public boolean checkIpLike();
}
