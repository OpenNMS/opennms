package org.opennms.server;

import org.opennms.client.*;

import java.util.*;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class InstallServiceImpl extends RemoteServiceServlet implements InstallService {
	public boolean checkOwnershipFileExists() {
		return false;
	}
	
	public String getOwnershipFilename(){
		return "fake_file_282829034.txt";
	}
	
	public void resetOwnershipFilename() {
		
	}
	
	public void setAdminPassword(String password) {
		
	}
	
	public boolean connectToDatabase() {
		return false;
	}
	
	public void setDatabaseConfig(String arguments){
		
	}
	
	public List<LoggingEvent> getDatabaseUpdateLogs(int offset){
		return Arrays.asList(new LoggingEvent[] {
			new LoggingEvent(),
			new LoggingEvent(),
			new LoggingEvent()
		});
	}
	
	public void updateDatabase() {
		
	}
	
	public boolean checkIpLike() {
		return false;
	}
}
