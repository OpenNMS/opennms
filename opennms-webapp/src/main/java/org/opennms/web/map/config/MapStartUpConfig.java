package org.opennms.web.map.config;

import org.opennms.web.map.MapsConstants;

public class MapStartUpConfig {
    
	int screenWidth=800;
    int screenHeight=600;
    int refreshTime=0;
    boolean fullScreen=false;

    int mapToOpenId=MapsConstants.MAP_NOT_OPENED;

    String user = null;
    
	public String getUser() {
		return user;
	}


	public void setUser(String user) {
		this.user = user;
	}


	public int getMapToOpenId() {
		return mapToOpenId;
	}


	public void setMapToOpenId(int mapToOpenId) {
		this.mapToOpenId = mapToOpenId;
	}


	public MapStartUpConfig() {
		super();
	}
	
	
	public MapStartUpConfig(String user, int screenWidth, int screenHeight, int refreshTime, boolean fullScreen, int mapToOpenId) {
		super();
		this.user = user;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.refreshTime = refreshTime;
		this.fullScreen = fullScreen;
		this.mapToOpenId = mapToOpenId;
	}

	
	
	public MapStartUpConfig(String user, int screenWidth, int screenHeight, int refreshTime, boolean fullScreen) {
		super();
		this.user = user;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.refreshTime = refreshTime;
		this.fullScreen = fullScreen;
	}


	
	public MapStartUpConfig(String user, int screenWidth, int screenHeight, int refreshTime) {
		super();
		this.user = user;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.refreshTime = refreshTime;
	}


	public boolean getFullScreen() {
		return fullScreen;
	}
	public void setFullScreen(boolean fullScreen) {
		this.fullScreen = fullScreen;
	}
	public int getRefreshTime() {
		return refreshTime;
	}
	public void setRefreshTime(int refreshTime) {
		this.refreshTime = refreshTime;
	}
	public int getScreenHeight() {
		return screenHeight;
	}
	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}
	public int getScreenWidth() {
		return screenWidth;
	}
	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}

    

}
