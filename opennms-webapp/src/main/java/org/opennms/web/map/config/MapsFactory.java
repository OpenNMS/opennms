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
 * Created: January 17, 2007
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

import java.util.HashMap;


/**
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public class MapsFactory {
	String managerClass;
	String dataSource;
	boolean adminModify;
	boolean allModify;
	boolean reload;
	boolean contextMenu;
	boolean doubleClick;
	HashMap param;
	String label;
	ContextMenu contMenu;
	
	public MapsFactory( String managerClass, boolean adminModify, boolean allModify,boolean reload, boolean contextMenu, ContextMenu contMenu, boolean doubleClick, String dataSource, HashMap param, String label) {
		super();
		this.managerClass = managerClass;
		this.dataSource = dataSource;
		this.adminModify=adminModify;
		this.allModify=allModify;
		this.reload = reload;
		this.contextMenu = contextMenu;
		this.doubleClick = doubleClick;
		this.param = param;
		this.label=label;
		this.contMenu=contMenu;
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getManagerClass() {
		return managerClass;
	}
	public void setManagerClass(String managerClass) {
		this.managerClass = managerClass;
	}
	public HashMap getParam() {
		return param;
	}
	public void setParam(HashMap param) {
		this.param = param;
	}
	public String getDataSource() {
		return dataSource;
	}
	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}
	public boolean isAdminModify() {
		return adminModify;
	}
	public void setAdminModify(boolean adminmodify) {
		this.adminModify = adminmodify;
	}
	public boolean isAllModify() {
		if(adminModify)
			return allModify;
		else return false;
	}
	public void setAllModify(boolean allModify) {
		this.allModify = allModify;
	}
	public boolean isReload() {
		return reload;
	}
	public void setReload(boolean reload) {
		this.reload = reload;
	}

	public boolean isContextMenu() {
		return contextMenu;
	}

	public void setContextMenu(boolean contextMenu) {
		this.contextMenu = contextMenu;
	}

	public boolean isDoubleClick() {
		return doubleClick;
	}

	public void setDoubleClick(boolean doubleClick) {
		this.doubleClick = doubleClick;
	}
	
	public ContextMenu getContMenu() {
		return contMenu;
	}
	
	public void setContMenu(ContextMenu contMenu) {
		this.contMenu = contMenu;
	}
}
