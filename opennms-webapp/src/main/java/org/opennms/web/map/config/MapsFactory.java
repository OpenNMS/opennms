package org.opennms.web.map.config;

import java.util.HashMap;

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
