//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.web.map;

/*
 * Created on 6-giu-2007
 *
 */

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Category;

import org.opennms.core.utils.ThreadCategory;

import org.opennms.web.map.MapsConstants;
import org.opennms.web.map.config.MapPropertiesFactory;
import org.opennms.web.map.config.MapStartUpConfig;
import org.opennms.web.map.view.*;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;


/**
 * @author mmigliore
 * 
 */
public class MapStartUpController extends SimpleFormController {
	Category log;

	private Manager manager;
	
	private MapsConstants mapsConstants;
	
	private MapPropertiesFactory mapsPropertiesFactory;
	
	public MapPropertiesFactory getMapsPropertiesFactory() {
		return mapsPropertiesFactory;
	}

	public void setMapsPropertiesFactory(MapPropertiesFactory mapsPropertiesFactory) {
		this.mapsPropertiesFactory = mapsPropertiesFactory;
	}

	public Manager getManager() {
		return manager;
	}

	public void setManager(Manager manager) {
		this.manager = manager;
	}
	

	public MapsConstants getMapsConstants() {
		return mapsConstants;
	}

	public void setMapsConstants(MapsConstants mapsConstants) {
		this.mapsConstants = mapsConstants;
	}

	protected ModelAndView onSubmit(Object arg0) throws Exception {
		
		MapStartUpConfig config = (MapStartUpConfig) arg0;
		manager.setMapStartUpConfig(config);
		manager.setAdminMode(false);
		Map<String, Object> models = new HashMap<String, Object> ();
		models.put("manager", manager);
		models.put("mapsConstants", mapsConstants);
		models.put("mapsPropertiesFactory", mapsPropertiesFactory);
		return new ModelAndView("/map/map",models);
	}
	
	public Object formBackingObject(HttpServletRequest request) throws Exception {
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());

		String user = request.getRemoteUser();
		
		boolean fullscreen = Boolean.parseBoolean(request.getParameter("fullscreen"));
		
		int refresh = Integer.parseInt(request.getParameter("refresh"));
		
		String dimension = request.getParameter("dimension");
	
		String mapToOpen = request.getParameter("mapToOpen");
		
		int mapToOpenId = MapsConstants.MAP_NOT_OPENED;
		if (mapToOpen != null) mapToOpenId = Integer.parseInt(mapToOpen); 

		if (log.isDebugEnabled()) 
			log.debug("StartUpValues: user/dimension/mapToOpen/refresh/fullscreen:" + user +"/" + dimension +"/" + mapToOpen +"/" + refresh +"/" + fullscreen );
		String[] dim = dimension.split("x");
		int mapwidth=Integer.parseInt(dim[0]);
		int mapheight=Integer.parseInt(dim[1]);
	
		MapStartUpConfig config = new MapStartUpConfig(user, mapwidth,mapheight,refresh,fullscreen,mapToOpenId); 
		return config;	
	}

}