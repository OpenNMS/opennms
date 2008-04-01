/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 6, 2007
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.map.config;

import org.opennms.web.map.MapsConstants;


/**
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public class MapStartUpConfig {
    
	int screenWidth=800;
    int screenHeight=600;
    int refreshTime=0;
    boolean fullScreen=false;
    
    boolean isAdminRole=false;
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
	
	
	public MapStartUpConfig(String user, boolean isAdminRole,int screenWidth, int screenHeight, int refreshTime, boolean fullScreen, int mapToOpenId) {
		super();
		this.user = user;
		this.isAdminRole = isAdminRole;
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


	public boolean isAdminRole() {
		return isAdminRole;
	}


	public void setAdminRole(boolean isAdminRole) {
		this.isAdminRole = isAdminRole;
	}

    

}
