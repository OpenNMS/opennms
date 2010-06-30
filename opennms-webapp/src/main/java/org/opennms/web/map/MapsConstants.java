/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: October 31, 2006
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
package org.opennms.web.map;

/**
 * <p>MapsConstants class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author maumig
 * This class contains some constants for Maps application
 * @version $Id: $
 * @since 1.6.12
 */
public final class MapsConstants extends JSTLConstants{
    private static final long serialVersionUID = 1L;

    /** Constant <code>LOG4J_CATEGORY="OpenNMS.Map"</code> */
    public static final String LOG4J_CATEGORY = "OpenNMS.Map";
	
	/** Constant <code>MAP_NOT_OPENED=-1</code> */
	public static final int MAP_NOT_OPENED = -1;
	
	/** Constant <code>NEW_MAP=-2</code> */
	public static final int NEW_MAP = -2;
	
	/** Constant <code>NODE_TYPE="N"</code> */
	public static final String NODE_TYPE = "N";
	
	/** Constant <code>MAP_TYPE="M"</code> */
	public static final String MAP_TYPE= "M";
	
	/** Constant <code>DEFAULT_BACKGROUND_COLOR="ffffff"</code> */
	public static final String DEFAULT_BACKGROUND_COLOR = "ffffff";

	// load infos
	
	/** Constant <code>LOADMAPS_ACTION="LoadMaps"</code> */
	public static final String LOADMAPS_ACTION = "LoadMaps";
	
	/** Constant <code>LOADNODES_ACTION="LoadNodes"</code> */
	public static final String LOADNODES_ACTION = "LoadNodes";

	/** Constant <code>LOADMAPINFO_ACTION="LoadMapInfo"</code> */
	public static final String LOADMAPINFO_ACTION = "LoadMapInfo";
	
	/** Constant <code>LOAD_ELEM_INFO_ACTION="LoadElementInfo"</code> */
	public static final String LOAD_ELEM_INFO_ACTION = "LoadElementInfo";

	//map action
	
	/** Constant <code>NEWMAP_ACTION="NewMap"</code> */
	public static final String NEWMAP_ACTION = "NewMap";

	/** Constant <code>OPENMAP_ACTION="OpenMap"</code> */
	public static final String OPENMAP_ACTION = "OpenMap";
	
	/** Constant <code>CLOSEMAP_ACTION="CloseMap"</code> */
	public static final String CLOSEMAP_ACTION = "CloseMap";
	
	/** Constant <code>SAVEMAP_ACTION="SaveMap"</code> */
	public static final String SAVEMAP_ACTION = "SaveMap";
	
	/** Constant <code>DELETEMAP_ACTION="DeleteMap"</code> */
	public static final String DELETEMAP_ACTION = "DeleteMap";	

	/** Constant <code>CLEAR_ACTION="Clear"</code> */
	public static final String CLEAR_ACTION = "Clear";

	/** Constant <code>RELOAD_ACTION="Reload"</code> */
	public static final String RELOAD_ACTION = "Reload";

	/** Constant <code>REFRESH_ACTION="Refresh"</code> */
	public static final String REFRESH_ACTION = "Refresh";
	
	// element action (a map should be opened )
	
	/** Constant <code>ADDRANGE_ACTION="AddRange"</code> */
	public static final String ADDRANGE_ACTION = "AddRange";
	
	/** Constant <code>ADDNODES_ACTION="AddNodes"</code> */
	public static final String ADDNODES_ACTION = "AddNodes";

	/** Constant <code>ADDMAPS_ACTION="AddMaps"</code> */
	public static final String ADDMAPS_ACTION = "AddMaps";

	/** Constant <code>ADDNODES_BY_CATEGORY_ACTION="AddNodesByCategory"</code> */
	public static final String ADDNODES_BY_CATEGORY_ACTION = "AddNodesByCategory";
	
	/** Constant <code>ADDNODES_BY_LABEL_ACTION="AddNodesByLabel"</code> */
	public static final String ADDNODES_BY_LABEL_ACTION = "AddNodesByLabel";
	
	/** Constant <code>ADDNODES_WITH_NEIG_ACTION="AddNodesWithNeig"</code> */
	public static final String ADDNODES_WITH_NEIG_ACTION = "AddNodesWithNeig";

	/** Constant <code>ADDMAPS_WITH_NEIG_ACTION="AddMapsWithNeig"</code> */
	public static final String ADDMAPS_WITH_NEIG_ACTION = "AddMapsWithNeig";

	/** Constant <code>ADDNODES_NEIG_ACTION="AddNodesNeig"</code> */
	public static final String ADDNODES_NEIG_ACTION = "AddNodesNeig";

	/** Constant <code>ADDMAPS_NEIG_ACTION="AddMapsNeig"</code> */
	public static final String ADDMAPS_NEIG_ACTION = "AddMapsNeig";

	/** Constant <code>DELETENODES_ACTION="DeleteNodes"</code> */
	public static final String DELETENODES_ACTION = "DeleteNodes";

	/** Constant <code>DELETEMAPS_ACTION="DeleteMaps"</code> */
	public static final String DELETEMAPS_ACTION = "DeleteMaps";
	
	// action mode
	/** Constant <code>SWITCH_MODE_ACTION="SwitchMode"</code> */
	public static final String SWITCH_MODE_ACTION = "SwitchMode";
	
	// map roles
	/** Constant <code>ROLE_USER="RO"</code> */
	public static final String ROLE_USER = "RO";

	/** Constant <code>ROLE_ADMIN="RW"</code> */
	public static final String ROLE_ADMIN = "RW";
	
	

	/**
	 * setting value to have no refresh
	 */
	
	public static final int NO_REFRESH_TIME=-1;
	
	/**
	 * setting value to have continous refresh
	 */
	
	public static final int AUTO_REFRESH_TIME=0;
	
	
	/** Constant <code>COLOR_SEMAPHORE_BY_SEVERITY="S"</code> */
	public static final String COLOR_SEMAPHORE_BY_SEVERITY = "S";
	
	/** Constant <code>COLOR_SEMAPHORE_BY_STATUS="T"</code> */
	public static final String COLOR_SEMAPHORE_BY_STATUS = "T";
	
	/** Constant <code>COLOR_SEMAPHORE_BY_AVAILABILITY="A"</code> */
	public static final String COLOR_SEMAPHORE_BY_AVAILABILITY = "A";
	
	
	/**
	 * <p>Constructor for MapsConstants.</p>
	 */
	public MapsConstants() {
		super();
	}
}

