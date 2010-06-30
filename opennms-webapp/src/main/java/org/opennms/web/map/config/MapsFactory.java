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
 * <p>MapsFactory class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 * @since 1.6.12
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
	
	/**
	 * <p>Constructor for MapsFactory.</p>
	 *
	 * @param managerClass a {@link java.lang.String} object.
	 * @param adminModify a boolean.
	 * @param allModify a boolean.
	 * @param reload a boolean.
	 * @param contextMenu a boolean.
	 * @param contMenu a {@link org.opennms.web.map.config.ContextMenu} object.
	 * @param doubleClick a boolean.
	 * @param dataSource a {@link java.lang.String} object.
	 * @param param a {@link java.util.HashMap} object.
	 * @param label a {@link java.lang.String} object.
	 */
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
	
	/**
	 * <p>Getter for the field <code>label</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * <p>Setter for the field <code>label</code>.</p>
	 *
	 * @param label a {@link java.lang.String} object.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * <p>Getter for the field <code>managerClass</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getManagerClass() {
		return managerClass;
	}
	/**
	 * <p>Setter for the field <code>managerClass</code>.</p>
	 *
	 * @param managerClass a {@link java.lang.String} object.
	 */
	public void setManagerClass(String managerClass) {
		this.managerClass = managerClass;
	}
	/**
	 * <p>Getter for the field <code>param</code>.</p>
	 *
	 * @return a {@link java.util.HashMap} object.
	 */
	public HashMap getParam() {
		return param;
	}
	/**
	 * <p>Setter for the field <code>param</code>.</p>
	 *
	 * @param param a {@link java.util.HashMap} object.
	 */
	public void setParam(HashMap param) {
		this.param = param;
	}
	/**
	 * <p>Getter for the field <code>dataSource</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDataSource() {
		return dataSource;
	}
	/**
	 * <p>Setter for the field <code>dataSource</code>.</p>
	 *
	 * @param dataSource a {@link java.lang.String} object.
	 */
	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}
	/**
	 * <p>isAdminModify</p>
	 *
	 * @return a boolean.
	 */
	public boolean isAdminModify() {
		return adminModify;
	}
	/**
	 * <p>Setter for the field <code>adminModify</code>.</p>
	 *
	 * @param adminmodify a boolean.
	 */
	public void setAdminModify(boolean adminmodify) {
		this.adminModify = adminmodify;
	}
	/**
	 * <p>isAllModify</p>
	 *
	 * @return a boolean.
	 */
	public boolean isAllModify() {
		if(adminModify)
			return allModify;
		else return false;
	}
	/**
	 * <p>Setter for the field <code>allModify</code>.</p>
	 *
	 * @param allModify a boolean.
	 */
	public void setAllModify(boolean allModify) {
		this.allModify = allModify;
	}
	/**
	 * <p>isReload</p>
	 *
	 * @return a boolean.
	 */
	public boolean isReload() {
		return reload;
	}
	/**
	 * <p>Setter for the field <code>reload</code>.</p>
	 *
	 * @param reload a boolean.
	 */
	public void setReload(boolean reload) {
		this.reload = reload;
	}

	/**
	 * <p>isContextMenu</p>
	 *
	 * @return a boolean.
	 */
	public boolean isContextMenu() {
		return contextMenu;
	}

	/**
	 * <p>Setter for the field <code>contextMenu</code>.</p>
	 *
	 * @param contextMenu a boolean.
	 */
	public void setContextMenu(boolean contextMenu) {
		this.contextMenu = contextMenu;
	}

	/**
	 * <p>isDoubleClick</p>
	 *
	 * @return a boolean.
	 */
	public boolean isDoubleClick() {
		return doubleClick;
	}

	/**
	 * <p>Setter for the field <code>doubleClick</code>.</p>
	 *
	 * @param doubleClick a boolean.
	 */
	public void setDoubleClick(boolean doubleClick) {
		this.doubleClick = doubleClick;
	}
	
	/**
	 * <p>Getter for the field <code>contMenu</code>.</p>
	 *
	 * @return a {@link org.opennms.web.map.config.ContextMenu} object.
	 */
	public ContextMenu getContMenu() {
		return contMenu;
	}
	
	/**
	 * <p>Setter for the field <code>contMenu</code>.</p>
	 *
	 * @param contMenu a {@link org.opennms.web.map.config.ContextMenu} object.
	 */
	public void setContMenu(ContextMenu contMenu) {
		this.contMenu = contMenu;
	}
}
